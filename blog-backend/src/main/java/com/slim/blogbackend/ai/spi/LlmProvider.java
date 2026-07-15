package com.slim.blogbackend.ai.spi;

import com.slim.blogbackend.ai.dto.LlmChatRequest;
import com.slim.blogbackend.ai.dto.LlmChatResponse;
import com.slim.blogbackend.ai.enums.AiProviderEnum;

import java.util.function.Consumer;

public interface LlmProvider {

    AiProviderEnum provider();

    LlmChatResponse chat(LlmChatRequest request);

    default void streamChat(
            LlmChatRequest request,
            Consumer<String> onToken,
            Consumer<LlmChatResponse> onDone,
            Consumer<Throwable> onError
    ) {
        try {
            LlmChatResponse full = chat(request);
            if (full.getErrorCode() != null) {
                if (onError != null) onError.accept(new RuntimeException(full.getErrorCode() + ": " + full.getErrorMessage()));
                return;
            }
            String text = full.getText() == null ? "" : full.getText();
            if (onToken != null) {
                int step = 2;
                for (int i = 0; i < text.length(); i += step) {
                    int end = Math.min(text.length(), i + step);
                    onToken.accept(text.substring(i, end));
                    try {
                        Thread.sleep(20L);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (onDone != null) onDone.accept(full);
        } catch (Throwable t) {
            if (onError != null) {
                onError.accept(t);
            } else {
                throw t;
            }
        }
    }
}
