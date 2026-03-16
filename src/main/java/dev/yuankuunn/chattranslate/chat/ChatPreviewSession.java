package dev.yuankuunn.chattranslate.chat;

import dev.yuankuunn.chattranslate.ChatTranslateClient;
import dev.yuankuunn.chattranslate.translation.TranslationException;
import dev.yuankuunn.chattranslate.translation.TranslationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

public final class ChatPreviewSession {
    private static final int LEFT_OUTER_MARGIN = 4;
    private static final int RIGHT_OUTER_MARGIN = 10;
    private static final int INNER_HORIZONTAL_PADDING = 4;
    private static final int SELECTED_HORIZONTAL_PADDING = 2;
    private static final int BOX_COLOR = 0xC0101010;
    private static final int TITLE_COLOR = 0xFFE5E5E5;
    private static final int BODY_COLOR = 0xFFFFFFFF;
    private static final int ERROR_COLOR = 0xFFFF8A8A;
    private static final int SELECTED_BACKGROUND_COLOR = 0x80408CFF;
    private static final int SELECTED_TEXT_COLOR = 0xFFFFFFFF;

    private final OutgoingChatPipeline pipeline;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final AtomicLong requestIds = new AtomicLong();

    private volatile ChatPreviewState state = ChatPreviewState.idle();
    private ScheduledFuture<?> scheduledPreview;
    private CompletableFuture<TranslationResult> currentRequest;
    private String currentRequestedInput = "";
    private String appliedCandidateText = "";
    private String suppressedEditedText = "";
    private int selectedCandidateIndex;
    private List<CandidateHitBox> candidateHitBoxes = List.of();

    public ChatPreviewSession(OutgoingChatPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public synchronized void onTextEdited(String text) {
        this.cancelScheduledPreview();
        this.candidateHitBoxes = List.of();

        if (!this.suppressedEditedText.isEmpty() && this.suppressedEditedText.equals(text)) {
            this.suppressedEditedText = "";
            this.state = ChatPreviewState.idle();
            return;
        }
        this.suppressedEditedText = "";

        if (!this.appliedCandidateText.isEmpty() && !this.appliedCandidateText.equals(text)) {
            this.appliedCandidateText = "";
        }
        if (!this.appliedCandidateText.isEmpty() && this.appliedCandidateText.equals(text)) {
            this.state = ChatPreviewState.idle();
            return;
        }

        if (!this.pipeline.isEligibleMessage(text)) {
            this.state = ChatPreviewState.idle();
            return;
        }
        if (!this.pipeline.isEnabled()) {
            this.state = ChatPreviewState.idle();
            return;
        }

        Optional<Component> blockingReason = this.pipeline.getBlockingReason();
        if (blockingReason.isPresent()) {
            this.state = ChatPreviewState.error(0L, text, blockingReason.get());
            return;
        }

        this.selectedCandidateIndex = 1;
        long requestId = this.requestIds.incrementAndGet();
        this.state = ChatPreviewState.loading(requestId, text);
        this.scheduledPreview = this.pipeline.previewExecutor().schedule(
            () -> this.beginPreviewRequest(text, requestId),
            this.pipeline.debounceMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    public synchronized boolean interceptSend(String text, boolean addToHistory, Consumer<String> sendTranslatedMessage) {
        if (!this.pipeline.isEligibleMessage(text)) {
            return false;
        }
        if (!this.pipeline.isEnabled()) {
            return false;
        }
        if (!this.appliedCandidateText.isEmpty() && this.appliedCandidateText.equals(text)) {
            return false;
        }

        Optional<Component> blockingReason = this.pipeline.getBlockingReason();
        if (blockingReason.isPresent()) {
            this.notifyFailure(blockingReason.get().getString());
            return true;
        }

        if (this.state.status() == ChatPreviewState.Status.READY && text.equals(this.state.sourceText())) {
            sendTranslatedMessage.accept(this.getSelectedText(this.state, this.selectedCandidateIndex));
            return true;
        }

        return false;
    }

    public synchronized boolean handleKeyPressed(int keyCode, Consumer<String> applySelectedText) {
        if (this.state.status() != ChatPreviewState.Status.READY) {
            return false;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                this.moveSelection(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.moveSelection(1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                this.applyCandidate(this.selectedCandidateIndex, applySelectedText);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public synchronized boolean handleMouseClicked(double mouseX, double mouseY, int button, Consumer<String> applySelectedText) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || this.state.status() != ChatPreviewState.Status.READY) {
            return false;
        }

        for (CandidateHitBox hitBox : this.candidateHitBoxes) {
            if (hitBox.contains(mouseX, mouseY)) {
                this.applyCandidate(hitBox.candidateIndex(), applySelectedText);
                return true;
            }
        }
        return false;
    }

    public synchronized void handleMouseMoved(double mouseX, double mouseY) {
        if (this.state.status() != ChatPreviewState.Status.READY) {
            return;
        }

        for (CandidateHitBox hitBox : this.candidateHitBoxes) {
            if (hitBox.contains(mouseX, mouseY)) {
                this.selectedCandidateIndex = hitBox.candidateIndex();
                return;
            }
        }
    }

    public void render(GuiGraphics guiGraphics, Font font, EditBox inputBox) {
        ChatPreviewState currentState = this.state;
        if (currentState.status() == ChatPreviewState.Status.IDLE) {
            this.candidateHitBoxes = List.of();
            return;
        }

        int maxWidth = Math.max(120, inputBox.getWidth() - LEFT_OUTER_MARGIN - RIGHT_OUTER_MARGIN);
        int boxX = inputBox.getX() + LEFT_OUTER_MARGIN;
        int contentWidth = maxWidth - (INNER_HORIZONTAL_PADDING * 2);
        int boxHeight;

        List<CandidateRenderEntry> renderedCandidates = List.of();
        List<FormattedCharSequence> plainLines = List.of();
        if (currentState.status() == ChatPreviewState.Status.READY) {
            renderedCandidates = this.buildCandidateEntries(font, currentState, contentWidth);
            boxHeight = 18 + renderedCandidates.stream().mapToInt(CandidateRenderEntry::height).sum() + 6;
        } else {
            plainLines = this.buildPlainLines(font, currentState, contentWidth);
            boxHeight = 18 + (plainLines.size() * 10) + 6;
        }

        int boxY = Math.max(4, inputBox.getY() - boxHeight - 4);
        guiGraphics.fill(boxX, boxY, boxX + maxWidth, boxY + boxHeight, BOX_COLOR);
        guiGraphics.drawString(font, this.titleFor(currentState), boxX + INNER_HORIZONTAL_PADDING, boxY + 4, TITLE_COLOR, false);

        int y = boxY + 16;
        if (currentState.status() == ChatPreviewState.Status.READY) {
            List<CandidateHitBox> hitBoxes = new ArrayList<>();
            for (CandidateRenderEntry entry : renderedCandidates) {
                int top = y - 1;
                if (entry.index() == this.selectedCandidateIndex) {
                    guiGraphics.fill(
                        boxX + SELECTED_HORIZONTAL_PADDING,
                        top,
                        boxX + maxWidth - SELECTED_HORIZONTAL_PADDING,
                        top + entry.height(),
                        SELECTED_BACKGROUND_COLOR
                    );
                }
                int textColor = entry.index() == this.selectedCandidateIndex ? SELECTED_TEXT_COLOR : BODY_COLOR;
                for (FormattedCharSequence line : entry.lines()) {
                    guiGraphics.drawString(font, line, boxX + INNER_HORIZONTAL_PADDING, y, textColor, false);
                    y += 10;
                }
                hitBoxes.add(new CandidateHitBox(entry.index(), boxX, boxX + maxWidth, top, top + entry.height()));
            }
            this.candidateHitBoxes = List.copyOf(hitBoxes);
        } else {
            this.candidateHitBoxes = List.of();
            int color = currentState.status() == ChatPreviewState.Status.ERROR ? ERROR_COLOR : BODY_COLOR;
            for (FormattedCharSequence line : plainLines) {
                guiGraphics.drawString(font, line, boxX + INNER_HORIZONTAL_PADDING, y, color, false);
                y += 10;
            }
        }
    }

    public synchronized void close() {
        this.cancelScheduledPreview();
        this.candidateHitBoxes = List.of();
    }

    private void beginPreviewRequest(String text, long requestId) {
        this.startRequest(text, requestId).thenAccept(result -> this.minecraft.execute(() -> this.applySuccess(requestId, text, result)))
            .exceptionally(exception -> {
                this.minecraft.execute(() -> this.applyError(requestId, text, exception));
                return null;
            });
    }

    private synchronized CompletableFuture<TranslationResult> startRequest(String text, long requestId) {
        this.currentRequestedInput = text;
        this.currentRequest = this.pipeline.requestCandidates(text);
        this.state = ChatPreviewState.loading(requestId, text);
        return this.currentRequest;
    }

    private void applySuccess(long requestId, String sourceText, TranslationResult result) {
        ChatPreviewState currentState = this.state;
        if (currentState.requestId() != requestId || !sourceText.equals(currentState.sourceText())) {
            return;
        }
        this.selectedCandidateIndex = Math.clamp(this.selectedCandidateIndex, 0, result.candidates().size());
        this.state = ChatPreviewState.ready(requestId, sourceText, result.candidates(), result.primaryText());
    }

    private void applyError(long requestId, String sourceText, Throwable throwable) {
        ChatPreviewState currentState = this.state;
        if (currentState.requestId() != requestId || !sourceText.equals(currentState.sourceText())) {
            return;
        }
        this.state = ChatPreviewState.error(requestId, sourceText, Component.literal(this.toUserMessage(throwable)));
    }

    private synchronized void applyCandidate(int candidateIndex, Consumer<String> applySelectedText) {
        if (this.state.status() != ChatPreviewState.Status.READY || candidateIndex >= this.candidateOptionCount(this.state)) {
            return;
        }

        this.selectedCandidateIndex = candidateIndex;
        String selectedText = this.getSelectedText(this.state, candidateIndex);
        this.appliedCandidateText = selectedText;
        this.suppressedEditedText = selectedText;
        this.cancelScheduledPreview();
        this.state = ChatPreviewState.idle();
        applySelectedText.accept(selectedText);
    }

    private synchronized void moveSelection(int offset) {
        if (this.state.status() != ChatPreviewState.Status.READY || this.candidateOptionCount(this.state) == 0) {
            return;
        }
        this.selectedCandidateIndex = Math.floorMod(this.selectedCandidateIndex + offset, this.candidateOptionCount(this.state));
    }

    private synchronized void cancelScheduledPreview() {
        if (this.scheduledPreview != null) {
            this.scheduledPreview.cancel(false);
            this.scheduledPreview = null;
        }
    }

    private Component titleFor(ChatPreviewState state) {
        return switch (state.status()) {
            case READY -> Component.translatable("chattranslate.preview.candidates_hint");
            case LOADING -> Component.translatable("chattranslate.preview.loading");
            case ERROR -> Component.translatable("chattranslate.preview.error");
            case IDLE -> Component.empty();
        };
    }

    private List<CandidateRenderEntry> buildCandidateEntries(Font font, ChatPreviewState state, int maxTextWidth) {
        List<CandidateRenderEntry> entries = new ArrayList<>();
        int translatedCount = Math.min(state.candidates().size(), 3);
        for (int index = 0; index <= translatedCount; index++) {
            String toneLabel = this.resolveToneLabel(index);
            Component line = Component.literal((index + 1) + ". [" + toneLabel + "] " + this.getSelectedText(state, index));
            List<FormattedCharSequence> lines = font.split(line, maxTextWidth);
            entries.add(new CandidateRenderEntry(index, lines));
        }
        return entries;
    }

    private List<FormattedCharSequence> buildPlainLines(Font font, ChatPreviewState state, int maxTextWidth) {
        return switch (state.status()) {
            case LOADING -> font.split(Component.literal(state.sourceText()), maxTextWidth);
            case ERROR -> font.split(state.message(), maxTextWidth);
            case READY, IDLE -> List.of();
        };
    }

    private String getSelectedText(ChatPreviewState state, int candidateIndex) {
        int index = Math.clamp(candidateIndex, 0, this.candidateOptionCount(state) - 1);
        if (index == 0) {
            return state.sourceText();
        }
        return state.candidates().get(index - 1).text();
    }

    private int candidateOptionCount(ChatPreviewState state) {
        return state.candidates().size() + 1;
    }

    private String resolveToneLabel(int index) {
        return switch (index) {
            case 0 -> Component.translatable("chattranslate.tone.original").getString();
            case 1 -> Component.translatable("chattranslate.tone.normal").getString();
            case 2 -> Component.translatable("chattranslate.tone.casual").getString();
            case 3 -> Component.translatable("chattranslate.tone.formal").getString();
            default -> Component.translatable("chattranslate.preview.extra_candidate", index + 1).getString();
        };
    }

    private void notifyFailure(String message) {
        ChatTranslateClient.get().showClientMessage(Component.translatable("chattranslate.message.translation_failed", message));
    }

    private String toUserMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current instanceof java.util.concurrent.CompletionException) {
            current = current.getCause();
        }
        if (current instanceof TranslationException translationException) {
            return translationException.userMessage();
        }
        return current.getMessage() == null ? "Unexpected translation error." : current.getMessage();
    }

    private record CandidateRenderEntry(int index, List<FormattedCharSequence> lines) {
        private int height() {
            return this.lines.size() * 10;
        }
    }

    private record CandidateHitBox(int candidateIndex, int minX, int maxX, int minY, int maxY) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.minX && mouseX <= this.maxX && mouseY >= this.minY && mouseY <= this.maxY;
        }
    }
}
