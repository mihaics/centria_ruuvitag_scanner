//
// Created by ISOHAJA on 16.12.2016.
//

#include "RealClass.h"

#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))

RealClass::RealClass()
{

}

RealClass::~RealClass(){

}

void RealClass::calculate(unsigned char* data, int len)
{
    min = 100000000;
    max = -10000000;
    for(int i = 0; i < len; i++)
    {
        min = MIN(data[i],min);
        max = MAX(data[i],max);
    }
}

int RealClass::getMin(){
    return min;
}
int RealClass::getMax(){
    return max;
}