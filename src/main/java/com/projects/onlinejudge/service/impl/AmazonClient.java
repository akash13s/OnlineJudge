package com.projects.onlinejudge.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.projects.onlinejudge.constants.FileConstants;
import com.projects.onlinejudge.service.AwsS3Service;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class AmazonClient implements AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AmazonClient.class);

    private AmazonS3Client s3client;

    private TransferManager transferManager;

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucket}")
    private String bucket;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = new AmazonS3Client(credentials);
        this.transferManager = TransferManagerBuilder.standard().withS3Client(s3client).build();
    }

    @Override
    public boolean uploadFile(String key, MultipartFile multipartFile) {
        try {
            File file = convertMultiPartToFile(multipartFile);
            s3client.putObject(bucket, key, file);
            file.delete();
            downloadDirectory("PROB", FileConstants.LOCAL_PROBLEM_DIRECTORY);
        }
        catch (Exception e) {
            logger.error("Error = {} while uploading file", e.getMessage());
            return false;
        }
        return true;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @Override
    public boolean downloadFile(String key, String destinationFilePath) {
        try {
            S3Object s3object = s3client.getObject(bucket, key);
            S3ObjectInputStream inputStream = s3object.getObjectContent();
            FileUtils.copyInputStreamToFile(inputStream, new File(destinationFilePath));
        }
        catch (Exception e) {
            logger.error("Error = {} while downloading file", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean downloadDirectory(String key, String destinationDirectoryPath) {
        try {
            MultipleFileDownload download = transferManager.downloadDirectory(bucket,
                    key, new File(destinationDirectoryPath));
            download.waitForCompletion();
        } catch (AmazonServiceException | InterruptedException e) {
            logger.error("Error = {} while downloading directory", e.getMessage());
            return false;
        }
        return true;
    }


    @Override
    public boolean deleteFile(String key) {
        try {
            s3client.deleteObject(bucket, key);
        }
        catch (Exception e) {
            logger.error("Error = {} while deleting file", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteDirectory(String key) {
        try {
            ObjectListing objectListing = s3client.listObjects(bucket, key);
            objectListing.getObjectSummaries().forEach(objectSummary -> {
                s3client.deleteObject(bucket,  objectSummary.getKey());
            });
        }
        catch (Exception e) {
            logger.error("Error = {} while deleting directory",e.getMessage());
            return false;
        }
        return true;
    }


}
