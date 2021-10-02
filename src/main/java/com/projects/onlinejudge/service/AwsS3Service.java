package com.projects.onlinejudge.service;

import org.springframework.web.multipart.MultipartFile;

public interface AwsS3Service {

    public boolean uploadFile(String key, MultipartFile multipartFile);

    public boolean downloadFile(String key, String destinationFilePath);

    public boolean deleteFile(String key);
}
