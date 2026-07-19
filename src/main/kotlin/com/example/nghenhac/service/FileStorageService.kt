package com.example.nghenhac.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.util.*

@Service
class FileStorageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {

    @Value("\${r2.bucket.songs}")
    private lateinit var songBucket: String

    @Value("\${r2.bucket.covers}")
    private lateinit var coverBucket: String

    private fun uploadFile(file: MultipartFile, bucket: String): String {
        try {
            // Sửa lỗi ghi đè bằng cách sử dụng UUID
            val fileExtension = file.originalFilename?.substringAfterLast('.', "") ?: ""
            val uniqueId = UUID.randomUUID().toString()
            val objectName = if (fileExtension.isNotEmpty()) "$uniqueId.$fileExtension" else uniqueId

            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .contentType(file.contentType)
                .build()

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.inputStream, file.size))

            return objectName
        } catch (e: Exception) {
            throw RuntimeException("Lỗi khi upload file: ${e.message}")
        }
    }

    fun uploadSong(file: MultipartFile): String {
        return uploadFile(file, songBucket)
    }

    fun uploadCover(file: MultipartFile): String {
        return uploadFile(file, coverBucket)
    }

    private fun getPresignedUrl(objectName: String, bucket: String): String {
        try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .build()

            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build()

            val presignedRequest = s3Presigner.presignGetObject(presignRequest)
            return presignedRequest.url().toString()
        } catch (e: Exception) {
            throw RuntimeException("Lỗi khi lấy presigned URL: ${e.message}")
        }
    }

    fun getSongUrl(objectName: String): String {
        return getPresignedUrl(objectName, songBucket)
    }

    fun getCoverUrl(objectName: String): String {
        return getPresignedUrl(objectName, coverBucket)
    }
}