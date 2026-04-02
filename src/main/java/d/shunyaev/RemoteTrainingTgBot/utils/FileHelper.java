package d.shunyaev.RemoteTrainingTgBot.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
public class FileHelper {

    public String readFileAsString(String relativePathToResourceFile) {
        return readFileAsString(relativePathToResourceFile, UTF_8);
    }

    public String readFileAsString(File file) {
        return readFileAsString(file, UTF_8);
    }

    public String readFileAsString(String relativePathToResourceFile, Charset encoding) {
        relativePathToResourceFile = relativePathToResourceFile.replace("\\", "/");
        String content;
        try {
            content = IOUtils.toString(getResourceFileStream(relativePathToResourceFile), encoding);
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка при получении файлов по относительному пути в ресурсах: '" + relativePathToResourceFile + "'", e);
        }
        return content;
    }

    public String readFileAsString(File file, Charset encoding) {
        String result;
        try {
            result = FileUtils.readFileToString(file, encoding);
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка при чтении файла", e);
        }
        return result;
    }

    private InputStream getResourceFileStream(String pathToResourceFile) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(pathToResourceFile);
    }
}
