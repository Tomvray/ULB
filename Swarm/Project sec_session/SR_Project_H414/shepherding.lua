-- Use Shift + Click to select a robot
-- When a robot is selected, its variables appear in this editor

-- Use Ctrl + Click (Cmd + Click on Mac) to move a selected robot to a different location

-- These variables are part of the special logic that enables multi-robot opeation - do not alter this part!
is_sheep = false

-- Put your global variables here, after this line

function table.copy(t)
	local t2 = {}
	for key,value in pairs(t) do
      t2[key] = value
	end
	return t2
end

function vector_normalize(v)
    local length = math.sqrt(v.x * v.x + v.y * v.y)
    local v2 = {}
    v2.x = v.x / length
    v2.y = v.y / length
    return v2
end

--This function computes the necessary wheel speed to go in the direction of the desired angle.
function ComputeSpeedFromAngle(angle, speed)
    dotProduct = 0.0;
    KProp = 20;
    wheelsDistance = 0.14;
	 -- if the target angle is behind the robot, we just rotate, no forward motion
    if angle > math.pi/2 or angle < -math.pi/2 then
        dotProduct = 0.0;
    else
    -- else, we compute the projection of the forward motion vector with the desired angle
        forwardVector = {math.cos(0), math.sin(0)}
        targetVector = {math.cos(angle), math.sin(angle)}
        dotProduct = forwardVector[1]*targetVector[1]+forwardVector[2]*targetVector[2]
    end

	 -- the angular velocity component is the desired angle scaled linearly
    angularVelocity = KProp * angle;
    -- the final wheel speeds are compute combining the forward and angular velocities, with different signs for the left and right wheel.
    speeds = {dotProduct * speed - angularVelocity * wheelsDistance, dotProduct * speed + angularVelocity * wheelsDistance}

    return speeds
end

----Sheep behavior: Do not alter this part!------------------------------------------

function sheep_sense()
   sensor_information = {}
   -- check for the closest LED that is either green or blue
   led_perceived = false
    closest_led_distance = math.huge
    closest_led_color = nil
    closest_led_angle = nil
    for i = 1, #robot.colored_blob_omnidirectional_camera do
        if ((robot.colored_blob_omnidirectional_camera[i].color.red == 0) and (robot.colored_blob_omnidirectional_camera[i].color.blue == 0) and (robot.colored_blob_omnidirectional_camera[i].color.green == 255)) -- led is green
        or ((robot.colored_blob_omnidirectional_camera[i].color.red == 0) and (robot.colored_blob_omnidirectional_camera[i].color.blue == 255) and (robot.colored_blob_omnidirectional_camera[i].color.green == 0)) then -- led is blue
            led_perceived = true
            if (robot.colored_blob_omnidirectional_camera[i].distance < closest_led_distance) then
                closest_led_distance = robot.colored_blob_omnidirectional_camera[i].distance
                closest_led_color = table.copy(robot.colored_blob_omnidirectional_camera[i].color)
                closest_led_angle = robot.colored_blob_omnidirectional_camera[i].angle
	    end
	end
    end
	-- We treat each proximity reading as a vector. The value represents the length
	-- and the angle gives the angle corresponding to the reading, wrt the robot's coordinate system
	-- First, we sum all the vectors.
	accumul = { x=0, y=0 }
	for i = 1, 24 do 
		-- we calculate the x and y components given length and angle
		vec = {
			x = robot.proximity[i].value * math.cos(robot.proximity[i].angle),
			y = robot.proximity[i].value * math.sin(robot.proximity[i].angle)
		}
		-- we sum the vectors into a variable called accumul
		accumul.x = accumul.x + vec.x
		accumul.y = accumul.y + vec.y
	end
	-- we get length and angle of the final sum vector
	length = math.sqrt(accumul.x * accumul.x + accumul.y * accumul.y)
	angle = math.atan2(accumul.y, accumul.x)
    -- return information
    sensor_information.led_perceived = led_perceived
    sensor_information.closest_led_distance = closest_led_distance
    sensor_information.closest_led_color = closest_led_color
    sensor_information.closest_led_angle = closest_led_angle
	sensor_information.proximity_vector_length = length
	sensor_information.proximity_vector_angle = angle
    return sensor_information
end

SHEEP_MAX_SPEED = 20 -- maximum move speed for the sheep


--[[ Control function function for the sheep robots. ]]
function sheep_control()
	angle = 0
	-- SENSE
	sensor = sheep_sense()
	-- THINK
	if sensor.led_perceived and (sensor.closest_led_distance > 25) then	-- only act when at least one LED is perceived
		-- move towards green light, faster if the light is far away
		if (sensor.closest_led_color.red == 0) and (sensor.closest_led_color.blue == 0) and (sensor.closest_led_color.green == 255) then
			vector = {
				x = (math.cos(sensor.closest_led_angle)),
				y = (math.sin(sensor.closest_led_angle))
			}
		-- move away from blue light
		elseif (sensor.closest_led_color.red == 0) and (sensor.closest_led_color.blue == 255) and (sensor.closest_led_color.green == 0) then
			vector = {
				x = - math.cos(sensor.closest_led_angle),
				y = - math.sin(sensor.closest_led_angle)
			}
		else	-- stop if no blue or green LED is perceived
			vector = {x=0,y=0}	
		end
	elseif (sensor.closest_led_distance < 25) then -- move away from closest LED
		vector = {
				x = -(math.cos(sensor.closest_led_angle)),
				y = -(math.sin(sensor.closest_led_angle))
			}
	else	-- stop if no LED is perceived
		vector = {x=0,y=0}	
	end
	if sensor.proximity_vector_length > 0.2 then
		prox_vector = {
			x = math.cos(sensor.proximity_vector_angle),
			y = math.sin(sensor.proximity_vector_angle)
		}
		vector.x = vector.x - prox_vector.x
		vector.y = vector.y - prox_vector.y
	end
	-- ACT
	length = math.sqrt(vector.x * vector.x + vector.y * vector.y)
	 angle = math.atan2(vector.y, vector.x)
	speed = ComputeSpeedFromAngle(angle, SHEEP_MAX_SPEED)
	if length > 0 then
		robot.wheels.set_velocity(speed[1], speed[2])
	else 
		robot.wheels.set_velocity(0,0)
	end
end

-------------------------------------------------------------------------------------

 --[[ Control function function for the shepherd robots. ]]
function shepherd_control()

end

--[[ This function checks if the robot type, based on its ID. 
     It should be called from init and reset, but not from step ]]
function check_robot_type()
    -- special logic to enable multi-robot opeation - do not alter this part!
    if string.find(robot.id, "sheep") ~= nil then
        is_sheep    = true
        led_color = "yellow"
    else
    	is_sheep = false
    	led_color = "blue"
    end
    -- this line is to help you with visualizing the roles in the swarm
    -- comment or remove this line before performing your experiments
    robot.leds.set_all_colors(led_color)
    robot.colored_blob_omnidirectional_camera.enable()
end

--[[ This function is executed every time you press the 'execute' button ]]
function init()
   -- special logic to enable multi-robot opeation - do not alter this part!
   check_robot_type()
   -- put your code here, after this line
end

--[[ This function is executed at each time step
     It will execute the logic of the controller for each
     type of robot ]]
function step()
   -- special logic to enable multi-robot opeation - do not alter this part!
   if is_sheep then
        sheep_control()
   else
   	shepherd_control()
   end
end

--[[ This function is executed every time you press the 'reset'
     button in the GUI. It is supposed to restore the state
     of the controller to whatever it was right after init() was
     called. The state of sensors and actuators is reset
     automatically by ARGoS. ]]
function reset()
    -- special logic to enable multi-robot opeation - do not alter this part!
    check_robot_type()
   -- put your code here, after this line

end

--[[ This function is executed only once, when the robot is removed
     from the simulation ]]
function destroy()
   -- put your code here, after this line

end
