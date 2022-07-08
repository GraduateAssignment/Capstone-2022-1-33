#include "Arduino.h"
#include "keyword.h"

/* btn1 */
void takePicture(){
	/* Chattering */
	delayMicroseconds(2000);
	if(digitalRead(intr1)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,0);
	digitalWrite(ledB,0);
}
/* btn2 */
void takeOCR(){
	delayMicroseconds(2000);
	if(digitalRead(intr2)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,228);
	digitalWrite(ledB,0);
}
/* btn3 */
void findWay(){
	delayMicroseconds(2000);
	if(digitalRead(intr3)!=LOW) return;
	digitalWrite(ledR,0);
	digitalWrite(ledG,255);
	digitalWrite(ledB,0);
}
/* btn4 */
void toggleLED(){
	delayMicroseconds(2000);
	if(digitalRead(intr4)!=LOW) return;
	extern bool toggle;
	if(toggle == true){
		digitalWrite(ledR, 0);
		digitalWrite(ledG, 0);
		digitalWrite(ledB, 0);
	}
	toggle = !toggle;
}
/* UW */
double calcDistance(){
	digitalWrite(trig, LOW);
	delayMicroseconds(2);
	digitalWrite(trig, HIGH);
	delayMicroseconds(10);
	digitalWrite(trig, LOW);
	
	const unsigned long duration = pulseIn(echo, HIGH);
	//int distance = duration / 29 / 2;
	return (duration / 5.8);
}
/* vibration module */
void vibration(bool flag){
	if(flag){
		analogWrite(vibr, 64);
	}
	else{
		analogWrite(vibr,0);
	}
}
/* active buzzer */
void alarm(bool flag){
	if(flag){
		digitalWrite(buzz, HIGH);
	}
	else{
		digitalWrite(buzz, LOW);
	}
}
/* Servo */
void turnServo(Servo myServo, int dir){
	dir *= 90;
	myServo.write(dir);
}
/* Bluetooth */
void callBLE(char ch){
	extern Servo myServo;
	if(ch == 'a'){
      takePicture();
    }
    else if(ch == 'b'){
      takeOCR();
    }
    else if(ch == 'c'){
      findWay();
    }
    else if(ch == 'd'){
      toggleLED();
    }
	else if(ch == 'e'){
		turnServo(myServo, 2);
	}
	else if(ch == 'f'){
		turnServo(myServo, 0);
	}
	else if(ch == 'g'){
		turnServo(myServo, 1);
	}
}