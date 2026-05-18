package com.movie.recommendation.common.util;

import java.text.Normalizer;
import java.util.concurrent.ThreadLocalRandom;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public static String uniqueSlug(String input) {
        String base = slugify(input);
        String suffix = Integer.toHexString(ThreadLocalRandom.current().nextInt(0x100000, 0xFFFFFF));
        return base.isEmpty() ? suffix : base + "-" + suffix;
    }
}
