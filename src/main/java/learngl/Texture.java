package learngl.tools;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Loads and manages an OpenGL 2D texture from a resource file.
 * Supports wrapping, mipmapping, and filtering parameters.
 */
public class Texture {

    private final int id;
    private final int width;
    private final int height;

    /**
     * Loads a texture image from the given classpath resource path, creates an
     * OpenGL texture object, sets default parameters (REPEAT wrapping, linear
     * mipmapped minification), generates mipmaps, and releases CPU-side memory.
     *
     * @param path the classpath resource path to the image file
     */
    public Texture(String path) {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        ByteBuffer image;
        int w, h;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Texture file not found in resources: " + path);
            }

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();

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

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);

        STBImage.stbi_image_free(image);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Binds this texture to the GL_TEXTURE_2D target.
     */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * Unbinds the current GL_TEXTURE_2D texture.
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Deletes the OpenGL texture object.
     */
    public void cleanup() {
        glDeleteTextures(id);
    }

    /**
     * Returns the OpenGL texture ID.
     *
     * @return the texture handle
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the width of the loaded texture in pixels.
     *
     * @return the texture width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the loaded texture in pixels.
     *
     * @return the texture height
     */
    public int getHeight() {
        return height;
    }
}
