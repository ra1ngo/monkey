// GLEW нужно подключать до GLFW.
// GLEW
//#define GLEW_STATIC
#include <GL/glew.h>
// GLFW
#include <GLFW/glfw3.h>
// Включаем стандартные заголовки
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>

GLuint createShader(GLenum type, const char *source) {
    // Create the shader object
    GLuint shader = glCreateShader(type);
    // Load the shader source
    glShaderSource(shader, 1, &source, NULL);
    // Compile the shader
    glCompileShader(shader);


    GLint status;
    GLchar infoLog[512];
    glGetShaderiv(shader, GL_COMPILE_STATUS, &status);
    if(!status)
    {
        glGetShaderInfoLog(shader, 512, NULL, infoLog);
        std::cout << "ERROR::SHADER::COMPILATION_FAILED\n" << infoLog << std::endl;
    }

    return shader;
}

int main(void){
    GLFWwindow* window;

    // Инициализируем GLFW
    if( !glfwInit() ){
        fprintf( stderr, "Ошибка при инициализации GLFWn" );
        return -1;
    }

    //glfwWindowHint(GLFW_FSAA_SAMPLES, 4); // 4x Сглаживание
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Мы хотим использовать OpenGL 3.3
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // To make MacOS happy; should not be needed
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Мы не хотим старый OpenGL
	glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);    //Выключение возможности изменения размера окна

    // Открыть окно и создать в нем контекст OpenGL
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    //GLFWwindow* window = glfwCreateWindow(800, 600, "LearnOpenGL", null, null);
    if( window == NULL ){
        fprintf( stderr, "Невозможно открыть окно GLFW. Если у вас Intel GPU, то он не поддерживает версию 3.3. Попробуйте версию уроков для OpenGL 2.1.n" );
        glfwTerminate();
        return -1;
    }
    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    // Инициализируем GLEW
    GLenum err = glewInit();
    if (GLEW_OK != err){
      /* Problem: glewInit failed, something is seriously wrong. */
      fprintf(stderr, "Error: %s\n", glewGetErrorString(err));

    }
    fprintf(stdout, "Status: Using GLEW %s\n", glewGetString(GLEW_VERSION));

    //Viewport
    int width, height;
    glfwGetFramebufferSize(window, &width, &height);
    glViewport(0, 0, width, height);

    // Включим режим отслеживания нажатия клавиш, для проверки ниже
    //glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);


    /////////////////
    /////ШЕЙДЕРЫ/////
    /////////////////
    std::string shader_vertex_source =
        "#version 330 core\n"
        "layout (location = 0) in vec3 position;\n" // Устанавливаем позицию атрибута в 0
        "layout (location = 1) in vec3 color;\n" // А позицию переменной с цветом в 1
        "out vec3 vertexColor;\n"  // Передаем цвет во фрагментный шейдер
        "void main()\n"
        "{\n"
        "   gl_Position = vec4(position, 1.0f);\n"  // Напрямую передаем vec3 в vec4
        "   vertexColor = color;\n" // Устанавливаем значение выходной переменной в темно-красный цвет.
        "}\n";

    std::string shader_fragment_source =
        "#version 330 core\n"
        "in vec3 vertexColor;\n" // Входная переменная из вершинного шейдера (то же название и тот же тип)
        "out vec4 FragColor;\n"
        "uniform float alpha;\n"
        "void main()\n"
        "{\n"
        "   FragColor=vec4(vertexColor, alpha);\n"
        "}\n";
    //vec4(1, 0, 0.5, 1)


    const char *source;
    //int length;
    source = shader_vertex_source.c_str();
    //length = shader_vertex_source.size();
    GLuint vertexShader = createShader(GL_VERTEX_SHADER, source);
    //int length1;
    source = shader_fragment_source.c_str();
    //length1 = shader_fragment_source.size();
    GLuint fragmentShader = createShader(GL_FRAGMENT_SHADER, source);


    GLuint shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertexShader);
    glAttachShader(shaderProgram, fragmentShader);
    glLinkProgram(shaderProgram);

    //отладка
    GLint status;
    glGetProgramiv(shaderProgram, GL_LINK_STATUS, &status);
    if(status == GL_FALSE) {
        GLint length;
        glGetProgramiv(shaderProgram, GL_INFO_LOG_LENGTH, &length);
        std::vector<char> log(length);
        glGetProgramInfoLog(shaderProgram, length, &length, &log[0]);
        std::cerr << &log[0];
        return false;
    }

    //массив вершин прямоугольника
    GLfloat vertices[] = {
        // Позиции           // Цвета
         0.5f,  0.5f, 0.0f,  1.0f, 0.0f, 0.0f, // Верхний правый угол
         0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f, // Нижний правый угол
        -0.5f, -0.5f, 0.0f,  0.0f, 0.0f, 1.0f, // Нижний левый угол
        -0.5f,  0.5f, 0.0f,  1.0f, 0.0f, 1.0f // Верхний левый угол
    };
    GLuint indices[] = {  // Помните, что мы начинаем с 0!
        0, 1, 3,   // Первый треугольник
        1, 2, 3    // Второй треугольник
    };

    //Vertex Array Object!!!
    GLuint VAO;
    glGenVertexArrays(1, &VAO);
    glBindVertexArray(VAO);
    // 2. Копируем наши вершины в буфер для OpenGL
    GLuint VBO;
    glGenBuffers(1, &VBO);
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    // 3. Копируем наши индексы в в буфер для OpenGL
    GLuint IBO;
    glGenBuffers(1, &IBO);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);
    // 3. Устанавливаем указатели на вершинные атрибуты
    //glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, (GLvoid*)0);
    //glEnableVertexAttribArray(0);
    // Атрибут с координатами
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)0);
    glEnableVertexAttribArray(0);
    // Атрибут с цветом
    glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 6 * sizeof(GLfloat), (GLvoid*)(3* sizeof(GLfloat)));
    glEnableVertexAttribArray(1);
    // 4. Отвязываем VAO (НЕ EBO)
    glBindVertexArray(0);

    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(window)){
        /* Render here */
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Включаем блендинг
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GLint alphaLocation = glGetUniformLocation(shaderProgram, "alpha");
        glUseProgram(shaderProgram);
        glUniform1f(alphaLocation, 0.5f);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);

        glfwPollEvents(); /* Poll for and process events */
        // Сбрасываем буферы
        glfwSwapBuffers(window); /* Swap front and back buffers */
    }

    glfwTerminate();
    return 0;
}
