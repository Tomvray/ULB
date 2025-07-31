-- comments

--[[
multi-line
comments

--Variables
x = 10
y = "ciao"

--print (do not use print)
log("INFO : x" .. x)
logerr("ERROR : y" .. y)
]]

-- Put your global variables here

--[[ This function is executed every time you press the 'execute'
     button ]]
function init()
   -- put your code here
   robot.wheels.set_velocity(15, 15)
   --set color to random
   local colors = {"red", "green", "blue", "yellow", "white"}
   local random_color = colors[math.random(#colors)]
   robot.leds.set_all_colors(random_color) -- set all LEDs to random color
end

function detect_obst()
    -- This function is used to detect obstacles
    --front
    for i = 1, 5 do
        if robot.proximity[i].value > 0.5 then
            robot.leds.set_all_colors("blue")
            log("Obstacle detected in front")
        end
    end
    for i = 24, 21 do
        if robot.proximity[i].value > 0.5 then
            robot.leds.set_all_colors("blue")
            log("Obstacle detected in front")
        end
    end

    --back
    for i = 9, 16 do
        if robot.proximity[i].value > 0.5 then
            robot.leds.set_all_colors("green")
            log("Obstacle detected in back")
        end
    end
end

function print_neigh()
    robot.colored_blob_omnidirectional_camera.enable()
    -- This function is used to print the neighbors
    for i = 1, #robot.colored_blob_omnidirectional_camera do
        print("Blob " .. i .. ":")
        print("  Color: " .. robot.colored_blob_omnidirectional_camera[i].color)
    end
    robot.colored_blob_omnidirectional_camera.disable()
end

--[[ This function is executed at each time step
     It must contain the logic of your controller ]]
function step()
     -- set the speed of the wheels
    --detect_obst()
    print_neigh()
end


--[[ This function is executed every time you press the 'reset'
     button in the GUI. It is supposed to restore the state
     of the controller to whatever it was right after init() was
     called. The state of sensors and actuators is reset
     automatically by ARGoS. ]]
function reset()
   -- put your code here
   init()
end



--[[ This function is executed only once, when the robot is removed
     from the simulation ]]
function destroy()
   -- put your code here
end
