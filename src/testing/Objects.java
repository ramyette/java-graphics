package testing;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Objects {
    
    private long window;
    private int shaderProgram;
    private int vaoId;
    private int vboId;

    private String vertexShaderSource = 
        "#version 330 core\n" +
        "layout (location = 0) in vec3 aPos;\n" +
        "void main() {\n" +
        "   gl_Position = vec4(aPos, 1.0);\n" +
        "}\n";

    private String fragmentShaderSource =
        "#version 330 core\n" +
        "out vec4 FragColor;\n" +
        "void main() {\n" +
        "   FragColor = vec4(1.0, 0.5, 0.2, 1.0);\n" +
        "}\n";

    private float[] vertices = {
        -0.5f, -0.5f, 0.0f, // Left
         0.5f, -0.5f, 0.0f, // Right
         0.0f,  0.5f, 0.0f  // Top
    };

    private final int SCREEN_WIDTH = 1280;
    private final int SCREEN_HEIGHT = 720;

    public void run() {
        init();
        initShaders();
        initTriangle();
        loop();

        // Free resources after window closes
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteProgram(shaderProgram);
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Objects Testing", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create window");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }  

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void initShaders() {
        // Create and compile vertex shader (shape)
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling vertex shader: " + glGetShaderInfoLog(vertexShader));
        }

        // Create and compile fragment shader (color)
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling fragment shader: " + glGetShaderInfoLog(fragmentShader));
        }

        // Link shaders into a shader program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking shader program: " + glGetProgramInfoLog(shaderProgram));
        }

        // Delete shaders after link
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void initTriangle() {
        // Generate VAO and bind
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Generate VBO and bind it as the GL_ARRAY_BUFFER
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // Create a FloatBuffer of the vertices
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();

        // Load vertex data to the GPU
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        // Specify the layout of the vertex data
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void loop() {

        while (!glfwWindowShouldClose(window)) {
            // Clear the screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Use the shader program
            glUseProgram(shaderProgram);

            // Bind the VAO containing the triangle's vertex data
            glBindVertexArray(vaoId);

            // Draw the triangle (starting at index 0, 3 vertices total)
            glDrawArrays(GL_TRIANGLES, 0, 3);

            // Unbind the VAO
            glBindVertexArray(0);

            // Swap color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Objects().run();
    }
}
