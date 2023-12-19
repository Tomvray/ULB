@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

; Function Attrs: nounwind uwtable
define i32 @readInt() #0 {
  %x = alloca i32, align 4
  %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
  %2 = load i32, i32* %x, align 4
  ret i32 %2
}

declare i32 @__isoc99_scanf(i8*, ...) #1

; External declaration of the getchar function
declare i32 @getchar()
; External declaration of the putchar function
declare i32 @putchar(i32);

; External declaration for ramdomization
declare i32 @rand()
declare i32 @time(i32*)
declare void @srand(i32)

define i32 @getNumber(){
   entry:
      %0 = call i32 @rand()
      %1 = srem i32 %0, 100
      ret i32 %1
}

define i32 @main() {
    ;initialization of randomizer
    %1 = call i32 @time(i32* null)
    call void @srand(i32 %1)
    %2 = call i32 @getNumber()
    %guess = alloca i32
    store i32 %2, i32* %guess
    %i = alloca i32
    store i32 0, i32* %i
    %try = alloca i32
    br label %forLoop
   forLoop:
   	%3 = load i32, i32* %i
   	%4 = icmp slt i32 %3, 5
   	br i1 %4, label %innerFor, label %afterFor
   innerFor:
   	%5 = call i32 @readInt()
   	store i32 %5, i32* %try
      %6 = load i32, i32* %try
      %7 = load i32, i32* %guess
      %8 = icmp sgt i32 %6, %7
      br i1 %8, label %firstCond, label %testNext
   testNext:
      %9 = load i32, i32* %try
      %10 = load i32, i32* %guess
      %11 = icmp slt i32 %9, %10
      br i1 %11, label %secondCond, label %else
   firstCond:
   	;greater
   	call i32 @putchar(i32 45);-
      call i32 @putchar(i32 10);\n
      br label %end_for
    secondCond:
      ;lower
      call i32 @putchar(i32 43);+
      call i32 @putchar(i32 10);\n
      br label %end_for
   else:
      ;success
      call i32 @putchar(i32 79);O
      call i32 @putchar(i32 75);K
      call i32 @putchar(i32 10);\n
      ret i32 0
      br label %end_for ; never reached
   end_for:
   	%20 = load i32, i32* %i
   	%21 = add i32 %20, 1
   	store i32 %21, i32* %i
   	br label %forLoop
   afterFor:
   	;failure
      call i32 @putchar(i32 75);K
      call i32 @putchar(i32 79);O
      call i32 @putchar(i32 10);\n
      ret i32 0
}
