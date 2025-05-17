-- Constants
local STATE_EXPLORE = "explore"
local STATE_GO_TO_ITEM = "go_to_item"
local STATE_CARRY_TO_NEST = "carry_to_nest"
local STATE_DROP = "drop"
local STATE_RETURN = "return"

local MAX_SPEED = 10.0
local NEST_THRESHOLD = 0.3  -- darker ground
local ITEM_THRESHOLD = 0.4  -- red color detection

-- State variables
local current_state = STATE_EXPLORE
local item_picked = false
local claimed_item_angle = nil

function init()
    current_state = STATE_EXPLORE
    item_picked = false
    claimed_item_angle = nil
    robot.range_and_bearing.clear_data()
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

------------------------
-- Behavior Functions --
------------------------

function explore()
    local item = get_item_blob()
    if item and not is_item_claimed(item.angle) then
        -- Claim this item
        claimed_item_angle = item.angle
        broadcast_claim(item.angle)
        current_state = STATE_GO_TO_ITEM
    else
        random_walk()
    end
end

function go_to_item()
    local item = get_item_blob_near_angle(claimed_item_angle)
    if item then
        set_wheel_velocity_toward(item.angle)
        if item.distance < 8.0 and math.abs(item.angle) < 0.3 then
            robot.gripper.lock_positive()
            item_picked = true
            current_state = STATE_CARRY_TO_NEST
        end
    else
        -- Item lost or picked by another robot
        claimed_item_angle = nil
        current_state = STATE_EXPLORE
    end
end

function carry_to_nest()
    broadcast_carrying()
    if robot.motor_ground[1].value < NEST_THRESHOLD then
        current_state = STATE_DROP
    else
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
    claimed_item_angle = nil
    current_state = STATE_RETURN
end

function return_to_search()
    if robot.motor_ground[1].value > NEST_THRESHOLD then
        current_state = STATE_EXPLORE
    else
        random_walk()
    end
end

----------------------------
-- Communication Functions
----------------------------

function broadcast_claim(angle)
    robot.range_and_bearing.clear_data()
    robot.range_and_bearing.set_data(1, 1) -- Status: claiming
    local encoded_angle = math.floor((angle + math.pi) * 255 / (2 * math.pi))
    robot.range_and_bearing.set_data(2, encoded_angle)
end

function broadcast_carrying()
    robot.range_and_bearing.clear_data()
    robot.range_and_bearing.set_data(1, 2) -- Status: carrying item
end

function is_item_claimed(my_angle)
    for _, msg in ipairs(robot.range_and_bearing) do
        if msg.data[1] == 1 then -- someone is claiming
            local other_angle = (msg.data[2] * 2 * math.pi / 255) - math.pi
            if math.abs(angle_difference(my_angle, other_angle)) < 0.3 then
                return true
            end
        end
    end
    return false
end

-------------------------
-- Utility Functions
-------------------------

function get_item_blob()
    for _, blob in ipairs(robot.colored_blob_omnidirectional_camera) do
        if blob.color.red > ITEM_THRESHOLD then
            return blob
        end
    end
    return nil
end

function get_item_blob_near_angle(target_angle)
    for _, blob in ipairs(robot.colored_blob_omnidirectional_camera) do
        if blob.color.red > ITEM_THRESHOLD and math.abs(angle_difference(blob.angle, target_angle)) < 0.3 then
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

function random_walk()
    if math.random() < 0.1 then
        robot.wheels.set_velocity(math.random(-MAX_SPEED, MAX_SPEED), math.random(-MAX_SPEED, MAX_SPEED))
    else
        robot.wheels.set_velocity(MAX_SPEED * 0.5, MAX_SPEED * 0.5)
    end
end

function weighted_vector_angle(readings)
    local x, y = 0, 0
    for _, r in ipairs(readings) do
        x = x + r.value * math.cos(r.angle)
        y = y + r.value * math.sin(r.angle)
    end
    return math.atan2(y, x)
end

function angle_difference(a, b)
    local diff = a - b
    while diff > math.pi do diff = diff - 2 * math.pi end
    while diff < -math.pi do diff = diff + 2 * math.pi end
    return diff
end

function reset()
    init()
end

function destroy()
    -- Optional cleanup
end
