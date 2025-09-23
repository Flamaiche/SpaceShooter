package learnGL.tools;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Texture {

    private final int id;
    private final int width;
    private final int height;

    public Texture(String path) {
        // Générer un ID de texture
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Paramètres par défaut
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Charger l'image depuis les ressources
        ByteBuffer image;
        int w, h;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Texture file not found in resources: " + path);
            }

            // Lire l'InputStream en ByteBuffer
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();

            // Charger l'image avec STBImage
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer widthBuffer  = stack.mallocInt(1);
                IntBuffer heightBuffer = stack.mallocInt(1);
                IntBuffer channels     = stack.mallocInt(1);

                STBImage.stbi_set_flip_vertically_on_load(true);
                image = STBImage.stbi_load_from_memory(buffer, widthBuffer, heightBuffer, channels, 4);
                if (image == null) {
                    throw new RuntimeException("Failed to load texture file " + path + " : " + STBImage.stbi_failure_reason());
                }

                w = widthBuffer.get();
                h = heightBuffer.get();
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read texture file " + path, e);
        }

        this.width = w;
        this.height = h;

        // Envoyer l'image au GPU
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);

        // Libérer la mémoire CPU
        STBImage.stbi_image_free(image);

        // Débind la texture
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanup() {
        glDeleteTextures(id);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
