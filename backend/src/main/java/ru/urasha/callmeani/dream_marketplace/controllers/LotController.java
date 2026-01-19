package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.LotCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.LotDto;
import ru.urasha.callmeani.dream_marketplace.mappers.LotMapper;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.LotService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.repositories.RatingRepository;

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

import java.util.List;

@RestController
@RequestMapping("/api/lots")
public class LotController {

    private final LotService lotService;
    private final UserAccountService userAccountService;
    private final RatingRepository ratingRepository;

    public LotController(LotService lotService, UserAccountService userAccountService, RatingRepository ratingRepository) {
        this.lotService = lotService;
        this.userAccountService = userAccountService;
        this.ratingRepository = ratingRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<LotDto> create(@Valid @RequestBody LotCreateRequest request,
                                         @AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var lot = lotService.createLotFromVisualization(
            request.visualizationId(),
            user,
            request.title(),
            request.description(),
            request.price(),
            request.categoryId(),
            request.tagIds(),
            request.tagNames()
        );
        return ResponseEntity.ok(LotMapper.toDto(lot));
    }

    @GetMapping
    public ResponseEntity<List<LotDto>> listOpen() {
        var list = lotService.listOpen().stream()
            .map(lot -> LotMapper.toDto(
                lot,
                ratingRepository.averageForLot(lot.getId()),
                ratingRepository.countForLot(lot.getId())
            ))
            .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    public ResponseEntity<List<LotDto>> listOwn(@AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var list = lotService.listOwn(user).stream()
            .map(lot -> LotMapper.toDto(
                lot,
                ratingRepository.averageForLot(lot.getId()),
                ratingRepository.countForLot(lot.getId())
            ))
            .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotDto> get(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        var user = details == null ? null : userAccountService.findById(details.userId()).orElse(null);
        var lot = lotService.getLotForPublic(id, user);
        return ResponseEntity.ok(LotMapper.toDto(
                lot,
                ratingRepository.averageForLot(lot.getId()),
                ratingRepository.countForLot(lot.getId())
        ));
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> preview(@PathVariable Long id) {
        var lot = lotService.getLotForPreview(id);
        var dream = lot.getDreamRecord();
        var visualization = dream != null ? dream.getVisualization() : null;
        if (visualization == null || visualization.getFilePath() == null || visualization.getFilePath().isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Файл не найден");
        }

        var filePath = visualization.getFilePath();
        byte[] data;

        try {
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                var rest = new org.springframework.web.client.RestTemplate();
                var response = rest.getForEntity(URI.create(filePath), byte[].class);
                data = Optional.ofNullable(response.getBody()).orElse(new byte[0]);
            } else {
                Path path = Path.of(filePath);
                if (!Files.exists(path)) {
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Файл не найден");
                }
                data = Files.readAllBytes(path);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось загрузить файл", e);
        }

        byte[] watermarked = watermarkImage(data, "Dream Marketplace");

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(watermarked);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var lot = lotService.getLotForPublic(id, user);
        var dream = lot.getDreamRecord();
        var owner = dream != null ? dream.getUser() : null;
        if (owner != null && owner.getId().equals(user.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Владелец не может скачивать лот");
        }
        var visualization = dream != null ? dream.getVisualization() : null;
        if (visualization == null || visualization.getFilePath() == null || visualization.getFilePath().isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Файл не найден");
        }

        var filePath = visualization.getFilePath();
        byte[] data;
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        try {
            if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                var rest = new org.springframework.web.client.RestTemplate();
                var response = rest.getForEntity(URI.create(filePath), byte[].class);
                data = Optional.ofNullable(response.getBody()).orElse(new byte[0]);
                var contentType = response.getHeaders().getContentType();
                if (contentType != null) {
                    mediaType = contentType;
                }
            } else {
                Path path = Path.of(filePath);
                if (!Files.exists(path)) {
                    throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Файл не найден");
                }
                data = Files.readAllBytes(path);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось скачать файл", e);
        }

        String filename = "lot-" + lot.getId() + ".png";
        try {
            if (filePath.startsWith("http")) {
                String name = Path.of(URI.create(filePath).getPath()).getFileName().toString();
                if (!name.isBlank()) {
                    filename = name;
                }
            } else {
                String name = Path.of(filePath).getFileName().toString();
                if (!name.isBlank()) {
                    filename = name;
                }
            }
        } catch (Exception ignored) {
            // keep fallback filename
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
    }

    private byte[] watermarkImage(byte[] source, String watermarkText) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
            if (image == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Невозможно прочитать изображение");
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
            throw new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось создать водяной знак", e);
        }
    }
}
