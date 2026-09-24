package com.tadeo.fish_project.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tadeo.fish_project.entity.Image;
import com.tadeo.fish_project.exception.InvalidImageException;

/*
Turns uploaded files into images. The only place that knows how image data is checked and stored,
so storing images outside of the database later would only change this class and the Image entity.
*/
@Service
public class ImageService {

    // ImageIO format names of the accepted formats and the mime type they are served with
    private static final Map<String, String> supportedFormats = Map.of(
        "jpeg", "image/jpeg",
        "png", "image/png"
    );

    public Image createImage(MultipartFile file) {
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException("Failed to read uploaded image");
        }
        return new Image(data, detectMimeType(data));
    }

    /*
    Determines the format from the data itself, the file name and content type sent by the client can't be trusted.
    Only reads the header, the image is never decoded.
    */
    static String detectMimeType(byte[] data) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            Iterator<ImageReader> readers = (input != null) ? ImageIO.getImageReaders(input) : null;
            if (readers != null && readers.hasNext()) {
                String mimeType = supportedFormats.get(readers.next().getFormatName().toLowerCase());
                if (mimeType != null) return mimeType;
            }
        } catch (IOException e) {
            // Unreadable data is treated like an unsupported format
        }
        throw new InvalidImageException("Only JPEG and PNG images are supported");
    }
}
