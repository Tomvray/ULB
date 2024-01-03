@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

; Function Attrs: noinline nounwind optnone ssp uwtable
define i32 @readInt() #0 {
  %1 = alloca i32, align 4
  %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)
  %3 = load i32, i32* %1, align 4
  ret i32 %3
}

declare i32 @scanf(i8*, ...) #1

@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: nounwind uwtable
define void @println(i32 %x) #0 {
  %1 = alloca i32, align 4
  store i32 %x, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
  ret void
}

declare i32 @printf(i8*, ...) #1




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
