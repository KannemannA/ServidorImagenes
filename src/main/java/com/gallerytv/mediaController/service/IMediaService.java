package com.gallerytv.mediaController.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.List;

public interface IMediaService {
    public ResponseEntity<?> uploadImage(List<MultipartFile> images, HttpServletRequest request);
    public ResponseEntity<?> findImage(String name) throws MalformedURLException;
}
