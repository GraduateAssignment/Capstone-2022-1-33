#include "Arduino.h"
#include "keyword.h"

/* intr1 */
void toggleSonar(){
	delayMicroseconds(3000); /* Chattering */
	if(digitalRead(intr1)!=LOW) return;
	extern bool togSonar;
	togSonar = !togSonar;
}
/* intr2 */
void takePicture(){
	delayMicroseconds(3000);
	if(digitalRead(intr2)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,0);
	digitalWrite(ledB,0);
	Serial2.println(1);
}
/* intr3 */
void takeOCR(){
	delayMicroseconds(3000);
	if(digitalRead(intr3)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,228);
	digitalWrite(ledB,0);
	Serial2.println(2);
}
/* intr4 */
void findWay(){
	delayMicroseconds(3000);
	if(digitalRead(intr4)!=LOW) return;
	digitalWrite(ledR,0);
	digitalWrite(ledG,255);
	digitalWrite(ledB,0);
	Serial2.println(3);
}
/* intr5 */
void toggleLED(){
	delayMicroseconds(2000);
	if(digitalRead(intr5)!=LOW) return;
	extern bool togLED;
	if(togLED == true){
		digitalWrite(ledR, 0);
		digitalWrite(ledG, 0);
		digitalWrite(ledB, 0);
	}
	togLED = !togLED;
}
/* vibration module */
void vibration(bool flag){
	if(flag){
		analogWrite(vibr, 128);
		Serial.println("vibra on");
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
void turnServo(Servo servo, int dir){
	dir *= 90;
	servo.write(dir);
}
/* Bluetooth */
void callBLE(char ch){
	extern Servo servo;
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
		turnServo(servo, 2);
	}
	else if(ch == 'f'){
		turnServo(servo, 0);
	}
	else if(ch == 'g'){
		turnServo(servo, 1);
	}
	else if(ch == 'h'){
		toggleSonar();
	}
}