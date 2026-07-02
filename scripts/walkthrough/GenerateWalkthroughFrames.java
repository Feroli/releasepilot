import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GenerateWalkthroughFrames {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final Color BACKGROUND_TOP = new Color(11, 18, 32);
    private static final Color BACKGROUND_BOTTOM = new Color(15, 23, 42);
    private static final Color PANEL = new Color(17, 24, 39, 225);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color ACCENT = new Color(56, 189, 248);
    private static final Color GREEN = new Color(34, 197, 94);

    private record Scene(String title, String[] lines, String footer) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected output directory");
        }
        File output = new File(args[0]);
        if (!output.exists() && !output.mkdirs()) {
            throw new IOException("Could not create " + output);
        }
        Scene[] scenes = scenes();
        for (int i = 0; i < scenes.length; i++) {
            BufferedImage image = render(scenes[i], i + 1, scenes.length);
            ImageIO.write(image, "png", new File(output, "slide%02d.png".formatted(i + 1)));
        }
    }

    private static BufferedImage render(Scene scene, int number, int total) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, WIDTH, HEIGHT, BACKGROUND_BOTTOM));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        drawGrid(g);

        g.setColor(PANEL);
        g.fill(new RoundRectangle2D.Double(60, 60, 1160, 600, 28, 28));
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(51, 65, 85));
        g.draw(new RoundRectangle2D.Double(60, 60, 1160, 600, 28, 28));

        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        g.setColor(Color.WHITE);
        g.drawString(scene.title(), 110, 150);

        g.setFont(new Font("SansSerif", Font.PLAIN, 32));
        g.setColor(TEXT);
        int y = 230;
        for (String line : scene.lines()) {
            for (String wrapped : wrap(g, line, 980)) {
                g.drawString(wrapped, 130, y);
                y += 44;
            }
            y += 12;
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.setColor(MUTED);
        g.drawString(scene.footer(), 110, 615);

        int progressWidth = (int) (1080 * (number / (double) total));
        g.setColor(new Color(51, 65, 85));
        g.fillRoundRect(100, 650, 1080, 10, 10, 10);
        g.setColor(ACCENT);
        g.fillRoundRect(100, 650, progressWidth, 10, 10, 10);

        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.setColor(GREEN);
        g.drawString("%02d / %02d".formatted(number, total), 1070, 115);
        g.dispose();
        return image;
    }

    private static void drawGrid(Graphics2D g) {
        g.setColor(new Color(30, 41, 59, 120));
        for (int x = 0; x < WIDTH; x += 64) {
            g.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += 64) {
            g.drawLine(0, y, WIDTH, y);
        }
    }

    private static List<String> wrap(Graphics2D g, String text, int maxWidth) {
        FontMetrics metrics = g.getFontMetrics();
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (metrics.stringWidth(candidate) <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static Scene[] scenes() {
        return new Scene[] {
                new Scene("ReleasePilot", new String[] {
                        "Java REST API for moving application versions through dev -> staging -> production.",
                        "The challenge focuses on the Promotion engine: DDD, CQRS, events, and ports."
                }, "Problem framing"),
                new Scene("Promotion Aggregate", new String[] {
                        "Promotion is not a passive persistence record.",
                        "It owns lifecycle transitions, acting users, timestamps, terminal immutability, and emitted domain events."
                }, "Where invariants live"),
                new Scene("Core Invariants", new String[] {
                        "Move exactly one step. Complete the source environment first.",
                        "Only one active promotion per application and target. Only approvers can approve. Terminal promotions are immutable."
                }, "Domain rules"),
                new Scene("Command Side", new String[] {
                        "Each command has a handler: request, approve, start deployment, complete, roll back, cancel.",
                        "Handlers load state, call the aggregate, persist changes, and record events."
                }, "CQRS write model"),
                new Scene("Query Side", new String[] {
                        "Read handlers return consumer-shaped DTOs.",
                        "Promotion detail with history, application status per environment, and paged promotion history."
                }, "CQRS read model"),
                new Scene("Events and Outbox", new String[] {
                        "Every successful transition records a domain event and stores it in the outbox transactionally.",
                        "The outbox publisher sends events to RabbitMQ after commit."
                }, "Reliable async handoff"),
                new Scene("Async Consumers", new String[] {
                        "The API responds before consumers finish.",
                        "Audit persists event type, promotion id, timestamp, and acting user. Release notes draft from PromotionApproved."
                }, "Queue-backed processing"),
                new Scene("Ports and Adapters", new String[] {
                        "Deployment, issue tracker, notifications, and approver lookup are explicit ports.",
                        "The challenge uses deterministic stubs, but the application core does not depend on real HTTP clients."
                }, "Integration boundaries"),
                new Scene("Verification", new String[] {
                        "Unit tests cover the aggregate and pipeline.",
                        "API tests run the full workflow and error cases. Testcontainers verifies PostgreSQL, RabbitMQ, async audit, and release notes."
                }, "Tests and confidence"),
                new Scene("Manual Review", new String[] {
                        "The manual-testing folder contains a Postman environment and collection.",
                        "It creates data, runs the happy path, checks read models, inspects async state, and runs negative cases."
                }, "Reviewer workflow"),
                new Scene("Trade-offs", new String[] {
                        "CQRS without full event sourcing keeps scope focused.",
                        "Outbox beats direct publish for reliability. The pipeline is fixed. Adapters are stubbed but replaceable."
                }, "Design decisions"),
                new Scene("What I Would Improve Next", new String[] {
                        "Add request idempotency keys. Promote read projections to separate tables.",
                        "Add richer deployment failure modeling. Replace stubs with real integrations and auth."
                }, "Closing")
        };
    }
}

