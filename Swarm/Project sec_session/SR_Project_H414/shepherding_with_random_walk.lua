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


--[[ Control function function for the CamBot robots. ]]
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
 
-----Shepherd behavior implementation-------------------------------------------------

-- Shepherd states
EXPLORE = 1
POSITION_BEHIND_SHEEP = 2
HERD_SHEEP = 3
AVOID_COLLISION = 4

-- Configuration parameters
SHEPHERD_MAX_SPEED = 15
SHEEP_DETECTION_RANGE = 100  -- maximum distance to detect sheep
POSITIONING_DISTANCE = 30    -- optimal distance behind sheep
COLLISION_THRESHOLD = 0.3    -- proximity sensor threshold for obstacles

-- State variables
shepherd_state = EXPLORE
target_sheep = nil
sheep_last_seen = 0
is_avoiding = false
avoiding_steps = 0

--[[ Detects sheep robots using the omnidirectional camera.
     Returns the closest sheep with its distance, angle, and color. ]]
function detect_sheep()
    local closest_sheep = nil
    local closest_distance = math.huge
    
    for i = 1, #robot.colored_blob_omnidirectional_camera do
        local blob = robot.colored_blob_omnidirectional_camera[i]
        -- Check if this is a yellow LED (sheep marker)
        if blob.color.red == 255 and blob.color.green == 255 and blob.color.blue == 0 then
            if blob.distance < closest_distance and blob.distance < SHEEP_DETECTION_RANGE then
                closest_distance = blob.distance
                closest_sheep = {
                    distance = blob.distance,
                    angle = blob.angle,
                    color = table.copy(blob.color)
                }
            end
        end
    end
    
    return closest_sheep
end

--[[ Detects the red LED target using the omnidirectional camera.
     Returns target information if found. ]]
function detect_red_target()
    local target = nil
    local closest_distance = math.huge
    
    for i = 1, #robot.colored_blob_omnidirectional_camera do
        local blob = robot.colored_blob_omnidirectional_camera[i]
        -- Check if this is a red LED (target marker)
        if blob.color.red == 255 and blob.color.green == 0 and blob.color.blue == 0 then
            if blob.distance < closest_distance then
                closest_distance = blob.distance
                target = {
                    distance = blob.distance,
                    angle = blob.angle
                }
            end
        end
    end
    
    return target
end

--[[ Checks for obstacles using proximity sensors.
     Returns true if collision avoidance is needed. ]]
function check_obstacles()
    for i = 1, 24 do
        if robot.proximity[i].value > COLLISION_THRESHOLD then
            return true
        end
    end
    return false
end

--[[ Implements obstacle avoidance using proximity sensors.
     Uses potential field approach to avoid obstacles. ]]
function avoid_obstacles()
    local avoidance_vector = {x = 0, y = 0}
    
    -- Sum all proximity vectors (repulsive forces)
    for i = 1, 24 do
        if robot.proximity[i].value > 0 then
            local force_magnitude = robot.proximity[i].value
            local obstacle_angle = robot.proximity[i].angle
            
            -- Create repulsive force away from obstacle
            avoidance_vector.x = avoidance_vector.x - force_magnitude * math.cos(obstacle_angle)
            avoidance_vector.y = avoidance_vector.y - force_magnitude * math.sin(obstacle_angle)
        end
    end
    
    -- Calculate movement angle and apply it
    local avoidance_angle = math.atan2(avoidance_vector.y, avoidance_vector.x)
    local speeds = ComputeSpeedFromAngle(avoidance_angle, SHEPHERD_MAX_SPEED * 0.7)
    robot.wheels.set_velocity(speeds[1], speeds[2])
end

--[[ Random exploration behavior when no sheep are detected. ]]
function explore_arena()
    if not is_avoiding then
        local obstacle = false
        -- Check front sensors for obstacles
        for i = 1, 4 do
            if robot.proximity[i].value > COLLISION_THRESHOLD then
                obstacle = true
                break
            end
        end
        for i = 20, 24 do
            if robot.proximity[i].value > COLLISION_THRESHOLD then
                obstacle = true
                break
            end
        end
        
        if obstacle then
            is_avoiding = true
            avoiding_steps = robot.random.uniform_int(10, 30)
        else
            robot.wheels.set_velocity(SHEPHERD_MAX_SPEED, SHEPHERD_MAX_SPEED)
        end
    else
        if avoiding_steps > 0 then
            -- Turn to avoid obstacle
            robot.wheels.set_velocity(-SHEPHERD_MAX_SPEED * 0.3, SHEPHERD_MAX_SPEED * 0.3)
            avoiding_steps = avoiding_steps - 1
        else
            is_avoiding = false
        end
    end
end

--[[ Calculates the desired position behind the sheep relative to the target.
     Returns the angle to move to get behind the sheep. ]]
function calculate_herding_position(sheep, target)
    if not target then
        -- If no target visible, position behind sheep in its current direction
        return sheep.angle + math.pi
    end
    
    -- Calculate angle from sheep to target
    local sheep_to_target_angle = target.angle
    
    -- Position behind sheep, opposite to target direction
    local desired_angle = sheep_to_target_angle + math.pi
    
    -- Calculate angle to reach desired position
    local position_angle = sheep.angle + (desired_angle - sheep.angle) * 0.3
    
    return position_angle
end

--[[ Main herding behavior - positions behind sheep and guides it toward target. ]]
function herd_sheep(sheep, target)
    if sheep.distance > POSITIONING_DISTANCE * 1.5 then
        -- Too far from sheep, approach it
        local speeds = ComputeSpeedFromAngle(sheep.angle, SHEPHERD_MAX_SPEED)
        robot.wheels.set_velocity(speeds[1], speeds[2])
        robot.leds.set_all_colors("blue")  -- Blue LED to repel if sheep turns around
        
    elseif sheep.distance < POSITIONING_DISTANCE * 0.7 then
        -- Too close to sheep, back away slightly
        local back_angle = sheep.angle + math.pi
        local speeds = ComputeSpeedFromAngle(back_angle, SHEPHERD_MAX_SPEED * 0.5)
        robot.wheels.set_velocity(speeds[1], speeds[2])
        robot.leds.set_all_colors("green")  -- Green LED to attract sheep toward target
        
    else
        -- At good distance, position behind sheep relative to target
        local herding_angle = calculate_herding_position(sheep, target)
        local speeds = ComputeSpeedFromAngle(herding_angle, SHEPHERD_MAX_SPEED * 0.8)
        robot.wheels.set_velocity(speeds[1], speeds[2])
        
        -- Choose LED color based on desired sheep movement
        if target and sheep.distance < 50 then
            robot.leds.set_all_colors("green")  -- Attract sheep toward target
        else
            robot.leds.set_all_colors("blue")   -- General herding with blue LED
        end
    end
end
------------------------------------------------------------------

 --[[ Main control function for shepherd robots.
      Implements a state machine with the following behaviors:
      1. EXPLORE: Random walk to find sheep
      2. POSITION_BEHIND_SHEEP: Move to optimal herding position
      3. HERD_SHEEP: Guide sheep toward the red LED target
      4. AVOID_COLLISION: Emergency obstacle avoidance ]]
function shepherd_control()
    -- SENSE: Gather sensor information
    local sheep = detect_sheep()
    local target = detect_red_target()
    local obstacles_detected = check_obstacles()
    
    -- Emergency collision avoidance takes highest priority
    if obstacles_detected and shepherd_state ~= AVOID_COLLISION then
        shepherd_state = AVOID_COLLISION
    end
    
    -- THINK: State machine logic
    if shepherd_state == EXPLORE then
        -- Look for sheep to herd
        if sheep then
            target_sheep = sheep
            sheep_last_seen = robot.system.time_in_seconds
            shepherd_state = POSITION_BEHIND_SHEEP
        else
            -- Continue exploring
            robot.leds.set_all_colors("black")  -- No LED signal while exploring
        end
        
    elseif shepherd_state == POSITION_BEHIND_SHEEP then
        -- Move to optimal position behind detected sheep
        if sheep then
            target_sheep = sheep
            sheep_last_seen = robot.system.time_in_seconds
            
            -- Check if we're in good herding position
            if sheep.distance >= POSITIONING_DISTANCE * 0.7 and 
               sheep.distance <= POSITIONING_DISTANCE * 1.5 then
                shepherd_state = HERD_SHEEP
            end
        else
            -- Lost sight of sheep
            if robot.system.time_in_seconds - sheep_last_seen > 3.0 then
                shepherd_state = EXPLORE
                target_sheep = nil
            end
        end
        
    elseif shepherd_state == HERD_SHEEP then
        -- Active herding of sheep toward target
        if sheep then
            target_sheep = sheep
            sheep_last_seen = robot.system.time_in_seconds
            
            -- Check if sheep is too far and we need to reposition
            if sheep.distance > POSITIONING_DISTANCE * 2.0 then
                shepherd_state = POSITION_BEHIND_SHEEP
            end
        else
            -- Lost sight of sheep
            if robot.system.time_in_seconds - sheep_last_seen > 2.0 then
                shepherd_state = EXPLORE
                target_sheep = nil
            end
        end
        
    elseif shepherd_state == AVOID_COLLISION then
        -- Emergency state - avoid obstacles
        if not obstacles_detected then
            -- Return to previous behavior
            if target_sheep then
                shepherd_state = POSITION_BEHIND_SHEEP
            else
                shepherd_state = EXPLORE
            end
        end
    end
    
    -- ACT: Execute behavior based on current state
    if shepherd_state == EXPLORE then
        explore_arena()
        
    elseif shepherd_state == POSITION_BEHIND_SHEEP then
        if target_sheep then
            -- Move toward sheep to get in herding position
            local approach_angle = target_sheep.angle
            local speeds = ComputeSpeedFromAngle(approach_angle, SHEPHERD_MAX_SPEED)
            robot.wheels.set_velocity(speeds[1], speeds[2])
            robot.leds.set_all_colors("blue")  -- Blue LED for general herding
        else
            shepherd_state = EXPLORE
        end
        
    elseif shepherd_state == HERD_SHEEP then
        if target_sheep then
            herd_sheep(target_sheep, target)
        else
            shepherd_state = EXPLORE
        end
        
    elseif shepherd_state == AVOID_COLLISION then
        avoid_obstacles()
        robot.leds.set_all_colors("red")  -- Red LED to signal emergency avoidance
        
    else
        -- Default: stop and reset to explore
        robot.wheels.set_velocity(0, 0)
        robot.leds.set_all_colors("black")
        shepherd_state = EXPLORE
    end
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
   -- Initialize shepherd state variables
   if not is_sheep then
       shepherd_state = EXPLORE
       target_sheep = nil
       sheep_last_seen = 0
       is_avoiding = false
       avoiding_steps = 0
   end
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
    -- Reset shepherd state variables
    if not is_sheep then
        shepherd_state = EXPLORE
        target_sheep = nil
        sheep_last_seen = 0
        is_avoiding = false
        avoiding_steps = 0
    end
end

--[[ This function is executed only once, when the robot is removed
     from the simulation ]]
function destroy()
   -- put your code here, after this line

end
