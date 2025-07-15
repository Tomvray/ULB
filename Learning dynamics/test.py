# SECTION 1: Imports
import numpy as np
import matplotlib.pyplot as plt
import egttools as egt
from tqdm.notebook import tqdm
import seaborn as sns

sns.set(style='whitegrid')

# SECTION 2: Parameters
N = 5
M = 100
r = 3
sigma = 1
beta_punishment = 1
gamma_cost = 0.3
delta = 0.0  # social coercion (punishing N)
alpha = 0.0  # second-order punishment (punishing C)
mu = 0.001

strategies = ['C', 'D', 'N', 'P']

# SECTION 3: Define payoff function
def get_payoff_matrix():
    P = np.zeros((4, 4))
    for i in range(4):
        for j in range(4):
            if i == 0:  # C
                if j == 1: P[i, j] = -1
                elif j == 3: P[i, j] = -1 - alpha * beta_punishment
            elif i == 1:  # D
                if j == 0: P[i, j] = r - 1
                elif j == 3: P[i, j] = -beta_punishment
            elif i == 2:  # N
                P[i, j] = sigma - delta * beta_punishment if j == 3 else sigma
            elif i == 3:  # P
                if j == 1: P[i, j] = r - 1 - gamma_cost
                elif j == 0: P[i, j] = r - 1 - alpha * gamma_cost
                elif j == 2: P[i, j] = sigma - delta * gamma_cost
                else: P[i, j] = r - 1
    return P

# SECTION 4: Build Markov chain and compute stationary distribution
betas = np.logspace(-5, 1, 30)
stationary_distributions = []

for beta in tqdm(betas):
    P_matrix = get_payoff_matrix()
    game = egt.games(P_matrix)
    pop = egt.Population(game, size=M)
    update = egt.PairwiseUpdate(beta=beta)
    markov = egt.MarkovProcess(pop, update, mutation=mu)
    stat_dist = markov.stationary_distribution()
    stationary_distributions.append(stat_dist)

stationary_distributions = np.array(stationary_distributions)

# SECTION 5: Plot stationary distribution vs β (Figures S1, S2)
colors = ['blue', 'red', 'gold', 'green']
labels = ['Cooperators (C)', 'Defectors (D)', 'Non-participants (N)', 'Punishers (P)']

plt.figure(figsize=(10, 6))
for i in range(4):
    plt.plot(betas, stationary_distributions[:, i], label=labels[i], color=colors[i])
plt.xscale('log')
plt.xlabel("Selection Strength β (log scale)")
plt.ylabel("Stationary Probability")
plt.title("Stationary Distribution vs Selection Strength")
plt.legend()
plt.tight_layout()
plt.show()

# SECTION 6: Plot Reduced Markov Chain (Figures 2/3 main paper)
import networkx as nx

beta = 0.25
P_matrix = get_payoff_matrix()
game = egt.games(P_matrix)
pop = egt.Population(game, size=M)
update = egt.PairwiseUpdate(beta=beta)
markov = egt.MarkovProcess(pop, update, mutation=mu)
T = markov.transition_matrix()

def plot_transition_graph(T, labels):
    G = nx.DiGraph()
    for i in range(len(labels)):
        for j in range(len(labels)):
            if i != j and T[i, j] > 0.01:
                G.add_edge(labels[i], labels[j], weight=T[i, j])
    pos = nx.spring_layout(G, seed=42)
    edge_weights = [G[u][v]['weight'] * 10 for u, v in G.edges()]
    nx.draw(G, pos, with_labels=True, node_color=colors, edge_color='gray',
            width=edge_weights, arrowsize=20, node_size=2000)
    plt.title("Reduced Markov Chain (β = {:.3f})".format(beta))
    plt.show()

plot_transition_graph(T, labels)
