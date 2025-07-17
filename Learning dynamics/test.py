import numpy as np
s_values = np.ones(7)
mult = 0.00001
for i in range(len(s_values)):
    s_values[i] = mult
    mult *= 10

print(s_values)