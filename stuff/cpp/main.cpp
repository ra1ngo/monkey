#include "monkey.h"

int main(void){

    Monkey *monk = new Monkey();
    Monkey::Screen *mainScreen = monk->createScreen();
    monk->setScreen(mainScreen);

    Monkey::Texture *texture1 = monk->createTexture("res/elli_walk.png", 1.1f, 0.5f);
    //std::cout << "Texture1: " << texture1->path << std::endl;
    Monkey::Texture *texture2 = monk->createTexture("res/img.jpg", 1.0f, 1.0f);
    //std::cout << "Texture1: " << texture1->path << std::endl;
    //std::cout << "Texture2: " << texture2->path << std::endl;


    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(mainScreen->context)){
        /* Render here */


        glEnable(GL_DEPTH_TEST); // включает использование буфера глубины
        glDepthFunc(GL_LEQUAL); // определяет работу буфера глубины: более ближние объекты перекрывают дальние
        //glDepthFunc(GL_LESS);// Фрагмент будет выводиться только в том, случае, если он находится ближе к камере, чем предыдущий

        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //texture1->drawRegion(10.2f, 10.2f, 50.5f, 50.5f); //это для пикселей
        texture1->drawRegion(0.25f, 0.25f, 0.25f, 0.25f); //это для процентов
        texture2->draw();
        //texture1->draw();

        glfwPollEvents();
        // Сбрасываем буферы
        glfwSwapBuffers(mainScreen->context);
    }

    glfwTerminate();
    return 0;
}
