package ru.urasha.callmeani.dream_marketplace.dto;

public record VisualizationUploadRequest(String filePath,
                                          String mime,
                                          Integer width,
                                          Integer height,
                                          Integer duration,
                                          String generator) {
}
