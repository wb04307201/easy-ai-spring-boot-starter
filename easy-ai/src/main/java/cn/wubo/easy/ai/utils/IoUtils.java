package cn.wubo.easy.ai.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class IoUtils {

    private IoUtils() {
    }

    /**
     * 默认缓存大小 8192
     */
    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;

    /**
     * 将字节数据写入指定的输出流中，完成后不关闭输出流。
     * 主要用于将字节数组内容复制到目标输出流。
     *
     * @param bytes 需要写入的字节数据。
     * @param os    目标输出流，用于接收字节数据。
     * @throws IOException 如果在写入过程中发生IO异常。
     */
    public static void writeToStream(byte[] bytes, OutputStream os) throws IOException {
        // 使用字节数组输入流包装字节数据，以便于复制到目标输出流
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            copy(is, os);  // 执行实际的数据复制操作
        }
    }

    /**
     * 拷贝流，拷贝后不关闭流。该方法首先尝试将输入流直接读取到输出流，特别适用于文件流的高效拷贝。
     * 对于非文件输入流，则采用标准的分块读写方式。
     *
     * @param is 输入流，可以是任意类型的输入流。
     * @param os 输出流，可以是任意类型的输出流。
     * @throws IOException 如果在读取或写入过程中发生IO错误。
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        if (is instanceof FileInputStream) {
            // 对于文件输入流，尝试直接读取全部内容以提高效率
            int available = is.available();
            byte[] result = new byte[available];
            int readLength = is.read(result);
            // 校验读取长度是否符合预期
            if (readLength != available) {
                throw new IOException(String.format("File length is [%s] but read [%s]!", available, readLength));
            }
            os.write(result);
        } else {
            // 对于非文件输入流，采用标准的分块读写方式
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            int len;
            // 循环读取直到没有更多数据
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
        }
        // 确保数据被刷新到输出流
        os.flush();
    }
}
