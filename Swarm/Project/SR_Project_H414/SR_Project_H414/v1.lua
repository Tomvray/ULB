-- Constants
local STATE_EXPLORE = "explore"
local STATE_GO_TO_ITEM = "go_to_item"
local STATE_CARRY_TO_NEST = "carry_to_nest"
local STATE_DROP = "drop"
local STATE_RETURN = "return"

local MAX_SPEED = 10.0
local NEST_THRESHOLD = 0.3  -- Threshold for detecting nest (darker grey)
local ITEM_THRESHOLD = 0.4  -- Threshold for detecting items by color

-- Variables
local current_state = STATE_EXPLORE
local item_picked = false

function init()
    current_state = STATE_EXPLORE
    robot.wheels.set_velocity(0, 0)
end

function step()
    if current_state == STATE_EXPLORE then
        explore()
    elseif current_state == STATE_GO_TO_ITEM then
        go_to_item()
    elseif current_state == STATE_CARRY_TO_NEST then
        carry_to_nest()
    elseif current_state == STATE_DROP then
        drop_item()
    elseif current_state == STATE_RETURN then
        return_to_search()
    end
end

------------------------------
-- STATE FUNCTIONS
------------------------------

function explore()
    local item = get_item_blob()
    if item then
        current_state = STATE_GO_TO_ITEM
    else
        random_walk()
    end
end

function go_to_item()
    local item = get_item_blob()
    if item then
        set_wheel_velocity_toward(item.angle)
        if item.distance < 20.0 then  -- Close enough to pick up
            robot.gripper.lock_grip()
            item_picked = true
            current_state = STATE_CARRY_TO_NEST
        end
    else
        current_state = STATE_EXPLORE
    end
end

function carry_to_nest()
    local nest = robot.motor_ground[1].value
    if nest < NEST_THRESHOLD then
        current_state = STATE_DROP
    else
        -- Move toward light (assuming nest is near light)
        local light = robot.light
        if #light > 0 then
            local angle = weighted_vector_angle(light)
            set_wheel_velocity_toward(angle)
        else
            random_walk()
        end
    end
end

function drop_item()
    robot.gripper.unlock()
    item_picked = false
    current_state = STATE_RETURN
end

function return_to_search()
    -- Move away from nest (bright area) for a few steps
    if robot.motor_ground[1].value > NEST_THRESHOLD then
        current_state = STATE_EXPLORE
    else
        random_walk()
    end
end

------------------------------
-- HELPER FUNCTIONS
------------------------------

function random_walk()
    local left = math.random(MAX_SPEED / 2, MAX_SPEED)
    local right = math.random(MAX_SPEED / 2, MAX_SPEED)
    if math.random() < 0.1 then
        left = -left
    end
    robot.wheels.set_velocity(left, right)
end

function get_item_blob()
    for _, blob in ipairs(robot.colored_blob_omnidirectional_camera) do
        if blob.color.red > ITEM_THRESHOLD then
            return blob
        end
    end
    return nil
end

function set_wheel_velocity_toward(angle)
    local k = 0.5
    local left = MAX_SPEED * (1 - k * angle)
    local right = MAX_SPEED * (1 + k * angle)
    robot.wheels.set_velocity(left, right)
end

function weighted_vector_angle(readings)
    local x, y = 0, 0
    for _, r in ipairs(readings) do
        x = x + r.value * math.cos(r.angle)
        y = y + r.value * math.sin(r.angle)
    end
    return math.atan2(y, x)
end

function reset()
    init()
end

function destroy()
    -- Cleanup if needed
end
