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

@y = constant i32 3

define i32 @exercise1() {
entry:
  %x = call i32 @readInt()
  %0 = load i32, i32* @y
  %1 = add i32 %x, i32 3
  %2 = sub i32 9 , i32 %0
  %3 = mul i32 %1 , %2
  ret i32 %3
}

define i32 @main() {
  entry:
  %0 = call i32 @exercise1()
  call void @println(i32 %0)
  ret i32 0
}
