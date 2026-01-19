package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.DreamService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationDto;
import ru.urasha.callmeani.dream_marketplace.mappers.VisualizationMapper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/visualizations")
public class VisualizationController {

    private final DreamService dreamService;
    private final UserAccountService userAccountService;

    public VisualizationController(DreamService dreamService, UserAccountService userAccountService) {
        this.dreamService = dreamService;
        this.userAccountService = userAccountService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/accept")
    public ResponseEntity<VisualizationDto> accept(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var vis = dreamService.acceptVisualization(id, user);
        return ResponseEntity.ok(VisualizationMapper.toDto(vis));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var vis = dreamService.getVisualizationForOwner(id, user);
        if (vis.getFilePath() == null || vis.getFilePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
        }

        byte[] data;
        try {
            if (vis.getFilePath().startsWith("http://") || vis.getFilePath().startsWith("https://")) {
                var rest = new org.springframework.web.client.RestTemplate();
                var response = rest.getForEntity(URI.create(vis.getFilePath()), byte[].class);
                data = Optional.ofNullable(response.getBody()).orElse(new byte[0]);
            } else {
                Path path = Path.of(vis.getFilePath());
                if (!Files.exists(path)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Файл не найден");
                }
                data = Files.readAllBytes(path);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить файл", e);
        }

        byte[] watermarked = watermarkImage(data, "Dream Marketplace");

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(watermarked);
    }

    private UserAccount requireUser(JwtUserDetails details) {
        return userAccountService.findById(details.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private byte[] watermarkImage(byte[] source, String watermarkText) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
            if (image == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Невозможно прочитать изображение");
            }

            int width = image.getWidth();
            int height = image.getHeight();
            BufferedImage watermarked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = watermarked.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.drawImage(image, 0, 0, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2d.setColor(new Color(255, 255, 255, 220));

            int fontSize = Math.max(18, Math.min(width, height) / 14);
            g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(watermarkText);
            int textHeight = fm.getHeight();

            double angle = Math.toRadians(-25);
            g2d.rotate(angle, width / 2.0, height / 2.0);

            int stepX = Math.max(200, textWidth + 80);
            int stepY = Math.max(140, textHeight + 60);

            for (int y = -height; y < height * 2; y += stepY) {
                for (int x = -width; x < width * 2; x += stepX) {
                    g2d.drawString(watermarkText, x, y);
                }
            }

            g2d.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(watermarked, "png", output);
            return output.toByteArray();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось создать водяной знак", e);
        }
    }
}
