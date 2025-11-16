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

@Service // Đánh dấu đây là một Service Bean
class FileStorageService(
    // Spring sẽ tự động "tiêm" MinioClient mà chúng ta đã tạo ở Bước 5
    private val minioClient: MinioClient
) {

    // Đọc tên các bucket từ application.properties
    @Value("\${minio.bucket.songs}")
    private lateinit var songBucket: String

    @Value("\${minio.bucket.covers}")
    private lateinit var coverBucket: String

    /**
     * Hàm chung để upload file.
     * @param file File người dùng tải lên.
     * @param bucket Tên bucket (songs-bucket hoặc covers-bucket).
     * @return Tên object duy nhất được tạo ra (ví dụ: "uuid-ten-file.mp3").
     */
    private fun uploadFile(file: MultipartFile, bucket: String): String {
        try {
            // 1. Tạo một tên file duy nhất để tránh trùng lặp
            val objectName = "${file.originalFilename}"

            // 2. Chuẩn bị request upload
            val putObjectArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1) // -1 để auto-detect part size
                .contentType(file.contentType) // Quan trọng để trình duyệt biết đây là file gì
                .build()

            // 3. Upload file lên MinIO
            minioClient.putObject(putObjectArgs)

            // 4. Trả về tên object đã upload
            return objectName
        } catch (e: Exception) {
            // Xử lý lỗi (ví dụ: log lại, ném ra exception tùy chỉnh)
            throw RuntimeException("Lỗi khi upload file: ${e.message}")
        }
    }

    /**
     * Hàm tiện ích để upload file bài hát
     */
    fun uploadSong(file: MultipartFile): String {
        return uploadFile(file, songBucket)
    }

    /**
     * Hàm tiện ích để upload ảnh bìa
     */
    fun uploadCover(file: MultipartFile): String {
        return uploadFile(file, coverBucket)
    }

    /**
     * Hàm chung để lấy URL truy cập file (có chữ ký - presigned URL).
     * URL này là TẠM THỜI, chỉ có hiệu lực trong một thời gian ngắn.
     * @param objectName Tên file lưu trên MinIO (lấy từ database).
     * @param bucket Tên bucket chứa file.
     * @return Một URL đầy đủ (ví dụ: "http://127.0.0.1:9000/songs-bucket/uuid-ten-file.mp3?...")
     */
    private fun getPresignedUrl(objectName: String, bucket: String): String {
        try {
            val args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET) // Chúng ta muốn lấy file
                .bucket(bucket)
                .`object`(objectName)
                .expiry(10, TimeUnit.MINUTES) // URL hết hạn sau 10 phút
                .build()

            return minioClient.getPresignedObjectUrl(args)
        } catch (e: Exception) {
            throw RuntimeException("Lỗi khi lấy presigned URL: ${e.message}")
        }
    }

    /**
     * Lấy URL để stream bài hát
     */
    fun getSongUrl(objectName: String): String {
        return getPresignedUrl(objectName, songBucket)
    }

    /**
     * Lấy URL để xem ảnh bìa
     */
    fun getCoverUrl(objectName: String): String {
        return getPresignedUrl(objectName, coverBucket)
    }
}