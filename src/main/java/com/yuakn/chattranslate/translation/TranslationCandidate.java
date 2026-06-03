package com.yuakn.chattranslate.translation;

public record TranslationCandidate(String toneLabel, String text) {
    public TranslationCandidate normalize() {
        return new TranslationCandidate(this.toneLabel == null ? "" : this.toneLabel.trim(), this.text == null ? "" : this.text.trim());
    }
}
