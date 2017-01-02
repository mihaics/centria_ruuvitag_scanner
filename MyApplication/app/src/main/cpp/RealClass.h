//
// Created by ISOHAJA on 16.12.2016.
//

#ifndef MYAPPLICATION_REALCLASS_H
#define MYAPPLICATION_REALCLASS_H


class RealClass
{
public:
    RealClass();
    ~RealClass();

    void calculate(unsigned char* data, int len);
    int getMin();
    int getMax();

private:
    int min;
    int max;

};


#endif //MYAPPLICATION_REALCLASS_H
