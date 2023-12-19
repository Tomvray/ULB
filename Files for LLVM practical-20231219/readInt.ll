@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

define i32 @readInt() {
  %x = alloca i32, align 4
  %1 = call i32 (i8*, ...) @__isoc99_scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
  %2 = load i32, i32* %x, align 4
  ret i32 %2
}

declare i32 @__isoc99_scanf(i8*, ...)
