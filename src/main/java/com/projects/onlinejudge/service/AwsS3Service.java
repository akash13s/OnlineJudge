package com.projects.onlinejudge.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface AwsS3Service {

    public String uploadFile(String problemCode, boolean isInputFile, int testCaseNumber, MultipartFile multipartFile);

    public void downloadFile(String key, String destinationFilePath);

    public void deleteFile(String key);
}
