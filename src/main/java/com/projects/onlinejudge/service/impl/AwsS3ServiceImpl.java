package com.projects.onlinejudge.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.service.AwsS3Service;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class AwsS3ServiceImpl implements AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);

    private AmazonS3 s3client;

    @Value("${aws.endpointUrl}")
    private String endpointUrl;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Override
    public String uploadFile(String problemCode, boolean isInputFile, int testCaseNumber,
                             MultipartFile multipartFile) {
        try {
            File file = convertMultiPartToFile(multipartFile);
            String problemKey = problemCode.concat(isInputFile? FileConstants.INPUT_FILE: FileConstants.OUTPUT_FILE);
            problemKey = problemKey.concat(testCaseNumber + FileConstants.TEXT_FILE_EXT);
            s3client.putObject(bucketName, problemKey, file);
            file.delete();
            return problemKey;
        }
        catch (Exception e) {
            logger.error("Error = {} while uploading file", e.getMessage());
        }
        return null;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @Override
    public void downloadFile(String key, String destinationFilePath) {
        try {
            S3Object s3object = s3client.getObject(bucketName, key);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            FileUtils.copyInputStreamToFile(inputStream, new File(destinationFilePath));
        }
        catch (Exception e) {
            logger.error("Error = {} while downloading file", e.getMessage());
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            s3client.deleteObject(bucketName, key);
        }
        catch (Exception e) {
            logger.error("Error = {} while deleting file", e.getMessage());
        }
    }

}
