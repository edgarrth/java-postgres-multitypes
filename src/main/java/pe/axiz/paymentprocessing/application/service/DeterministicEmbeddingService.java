package pe.axiz.paymentprocessing.application.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class DeterministicEmbeddingService {
    private static final int DIMENSIONS = 6;

    public List<Double> embed(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalize(text).getBytes(StandardCharsets.UTF_8));
            List<Double> vector = new ArrayList<>(DIMENSIONS);
            for (int i = 0; i < DIMENSIONS; i++) {
                int unsigned = Byte.toUnsignedInt(hash[i]);
                double normalized = (unsigned / 255.0d) * 2.0d - 1.0d;
                vector.add(round(normalized));
            }
            return vector;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }
}
