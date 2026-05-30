package learngl;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages the lifecycle of an OpenGL shader program consisting of a vertex
 * and a fragment shader. Shader source code is loaded from resource files,
 * compiled, linked, and exposed via uniform setter methods.
 */
public class Shader {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    private String vertexCode;
    private String fragmentCode;

    /**
     * Loads, compiles, and links a shader program from the given vertex and
     * fragment shader resource paths.
     *
     * @param vertexPath   the classpath resource path for the vertex shader source
     * @param fragmentPath the classpath resource path for the fragment shader source
     */
    public Shader(String vertexPath, String fragmentPath) {
        vertexCode = readFileFromResources(vertexPath);
        fragmentCode = readFileFromResources(fragmentPath);
        compile();
    }

    private String readFileFromResources(String fileName) {
        try (InputStream in = Shader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + fileName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error reading resource: " + fileName, e);
        }
    }

    /**
     * Returns the OpenGL program ID of this shader.
     *
     * @return the program handle
     */
    public int getProgramId() {
        return programId;
    }

    private void compile() {
        vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, vertexCode);
        glCompileShader(vertexShaderId);
        if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur compilation vertex shader : " + glGetShaderInfoLog(vertexShaderId));
        }

        fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, fragmentCode);
        glCompileShader(fragmentShaderId);
        if (glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur compilation fragment shader : " + glGetShaderInfoLog(fragmentShaderId));
        }

        programId = glCreateProgram();
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Erreur linkage shader : " + glGetProgramInfoLog(programId));
        }
    }

    /**
     * Activates this shader program for rendering.
     */
    public void bind() {
        glUseProgram(programId);
    }

    /**
     * Deactivates the current shader program.
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * Detaches and deletes the shader objects and program from OpenGL.
     */
    public void cleanup() {
        unbind();
        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        glDeleteProgram(programId);
    }

    /**
     * Sets a 4x4 matrix uniform in the shader.
     *
     * @param name   the uniform variable name
     * @param matrix the matrix value
     */
    public void setUniformMat4f(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    /**
     * Sets a float uniform in the shader.
     *
     * @param name  the uniform variable name
     * @param value the float value
     */
    public void setUniform1f(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    /**
     * Sets a vec2 uniform in the shader.
     *
     * @param name the uniform variable name
     * @param x    the first component
     * @param y    the second component
     */
    public void setUniform2f(String name, float x, float y) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform2f(location, x, y);
        }
    }

    /**
     * Sets a vec3 uniform in the shader.
     *
     * @param name the uniform variable name
     * @param x    the first component
     * @param y    the second component
     * @param z    the third component
     */
    public void setUniform3f(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        if (location != -1) {
            glUniform3f(location, x, y, z);
        }
    }
}
