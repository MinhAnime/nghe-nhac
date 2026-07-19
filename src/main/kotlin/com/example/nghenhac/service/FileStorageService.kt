package com.example.nghenhac.service

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class FileStorageService(
    private val minioClient: MinioClient
) {

    @Value("\${minio.bucket.songs}")
    private lateinit var songBucket: String

    @Value("\${minio.bucket.covers}")
    private lateinit var coverBucket: String

    @Value("\${minio.presigned-expiry-minutes}")
    private var presignedExpiryMinutes: Int = 10

    private fun uploadFile(file: MultipartFile, bucket: String): String {
        try {
            // Sửa lỗi ghi đè bằng cách sử dụng UUID
            val fileExtension = file.originalFilename?.substringAfterLast('.', "") ?: ""
            val uniqueId = UUID.randomUUID().toString()
            val objectName = if (fileExtension.isNotEmpty()) "$uniqueId.$fileExtension" else uniqueId

            val putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1) // -1 để tự động nhận diện part size
                .contentType(file.contentType)
                .build()

            minioClient.putObject(putObjectArgs)

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
            val args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(objectName)
                .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                .build()

            return minioClient.getPresignedObjectUrl(args)
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