package com.api.e_commerce.application.translation;

import com.api.e_commerce.domain.translation.TranslationService;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class LocalProductTranslationService implements TranslationService {
    private static final Map<String, String> CATEGORIES = Map.of(
            "electronics", "Eletrônicos",
            "jewelery", "Joias",
            "men's clothing", "Roupas Masculinas",
            "women's clothing", "Roupas Femininas"
    );

    @Override
    public String translate(String text) {
        return text == null ? null : text.trim();
    }

    @Override
    public String translateCategory(String category) {
        if (category == null || category.isBlank()) {
            return "Outros";
        }
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return CATEGORIES.getOrDefault(normalized, category.trim());
    }
}
