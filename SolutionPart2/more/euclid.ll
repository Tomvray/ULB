
define i32 @main() {
entry:
%a  = alloca i32
%0 = call i32 @readInt()
store i32 %0, i32* %a
%b  = alloca i32
%1 = call i32 @readInt()
store i32 %1, i32* %b
br label %while.cond0
while.cond0:
%2 = add i32 0, 0
%3 = load i32 , i32* %b
%4= icmp slt i32 %2, %3
br i1 %4, label %while.body0, label %while.end0
while.body0:
%5 = load i32 , i32* %b
%c  = alloca i32
store i32 %5, i32* %c
br label %while.cond1
while.cond1:
%6 = load i32 , i32* %b
%7 = load i32 , i32* %a
%8 = add i32 0, 1
%9 = add i32 %7, %8
%10= icmp slt i32 %6, %9
br i1 %10, label %while.body1, label %while.end1
while.body1:
%11 = load i32 , i32* %a
%12 = load i32 , i32* %b
%13 = sub i32 %11, %12
store i32 %13, i32* %a
br label %while.cond1
while.end1:
%14 = load i32 , i32* %a
store i32 %14, i32* %b
%15 = load i32 , i32* %c
store i32 %15, i32* %a
br label %while.cond0
while.end0:
%16 = load i32, i32* %a 
call void @println(i32 %16)
ret i32 0
}
