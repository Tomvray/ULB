-- Use Shift + Click to select a robot
-- When a robot is selected, its variables appear in this editor

-- Use Ctrl + Click (Cmd + Click on Mac) to move a selected robot to a different location



-- Put your global variables here
direction = "forward"


--[[ This function is executed every time you press the 'execute' button ]]
function init()
   -- put your code here
end



--[[ This function is executed at each time step
     It must contain the logic of your controller ]]
function step()
   -- put your code here*
	obstacle = 0
	for i = 1, 24, 1 do
		if robot.proximity[i].value > 0.1 then
			obstacle = 1
			log("obstacle")
		end
	end
	if obsatcle == 1 then
		if direction == 1 then
			direction = 0
			log("change direction")
		else
			direction = 1
		end
	end
	
	if direction == 1 then
		robot.wheels.set_velocity(5,5)	
	else
		log("backward")
		robot.wheels.set_velocity(-5,-5)	
	end
end



--[[ This function is executed every time you press the 'reset'
     button in the GUI. It is supposed to restore the state
     of the controller to whatever it was right after init() was
     called. The state of sensors and actuators is reset
     automatically by ARGoS. ]]
function reset()
   -- put your code here
end



--[[ This function is executed only once, when the robot is removed
     from the simulation ]]
function destroy()
   -- put your code here
end
