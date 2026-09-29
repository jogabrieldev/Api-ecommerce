package com.api.e_commerce.application.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalProductTranslationServiceTest {
    private final LocalProductTranslationService service = new LocalProductTranslationService();

    @Test
    void shouldTranslateKnownCategoriesAndPreserveUnknownText() {
        assertEquals("Eletrônicos", service.translateCategory(" electronics "));
        assertEquals("Roupas Masculinas", service.translateCategory("men's clothing"));
        assertEquals("Unknown", service.translateCategory(" Unknown "));
        assertEquals("Original title", service.translate(" Original title "));
    }
}
