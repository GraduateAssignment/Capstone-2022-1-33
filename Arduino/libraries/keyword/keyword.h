#ifndef MyLibrary_H
#define MyLibrary_H

#include <SoftwareSerial.h>
#include <Servo.h>

#define ledR 30
#define ledG 32
#define ledB 34
#define trig 43
#define echo 42
#define vibr 4
#define buzz 46
#define serv 48
#define intr1 2
#define intr2 3
#define intr3 20
#define intr4 21

void takePicture();
void takeOCR();
void findWay();
void toggleLED();
double calcDistance();
void vibration(bool);
void alarm(bool);
void turnServo(Servo, int);
void callBLE(char);

#endif