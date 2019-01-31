import numpy as np

cmd_use_serial = "-XX:+UseSerialGC -XX:-BackgroundCompilation"
cmd_runner = "-cp gtlibrary-jar-with-dependencies.jar cz.agents.gtlibrary.algorithms.cr.CRExperiments"

def _(s):
    for x in ":()[],. -":
        s = s.replace(x, "")
    return s

domain_params = {
    "B-RPS": "BRPS 0 100",
    "IIGS-5": "IIGS 0 5 true true",
    "LD-116": "LD 1 1 6",
    "GP-3322": "GP 3 3 2 2",
    "IIGS-13": "IIGS 0 13 true true",
    "LD-226": "LD 2 2 6",
    "GP-4644": "GP 4 6 4 4",
    "PTTT": "PTTT",
}


small_domains = [
    "B-RPS",
    "IIGS-5",
    "LD-116",
    "GP-3322",
]

big_domains = [
    "IIGS-13",
    "LD-226",
    "GP-4644",
    "PTTT",
]

algs = [
    "MCCFR",
    "MCCR",
    "OOS",
    "RND",
    "UCT",
    "RM"
]

max_utils = {
    "B-RPS": 100,
    "GP-3322": 37,
    "GP3322": 37,
    "GP": 37,
    "gp": 37,
    "GP-4644": 121,
    "GP4644": 121,
    "LD-116": 1,
    "LD": 1,
    "ld": 1,
    "LD116": 1,
    "LD-226": 1,
    "LD226": 1,
    "IIGS-4": 1,
    "IIGS-0-4": 1,
    "IIGS-5": 1,
    "IIGS": 1,
    "iigs": 1,
    "IIGS-0-5": 1,
    "IIGS05truetrue": 1,
    "IIGS-1-5": 1,
    "IIGS-0-13": 1,
    "IIGS013truetrue": 1,
    "IIGS-1-13": 1,
    "IIGS-13": 1,
    "PTTT": 1,
}

games = [
    "B-RPS",
    "GP-3322",
    "GP3322",
    "GP-4644",
    "GP4644",
    "GP",
    "LD-116",
    "LD116",
    "LD-226",
    "LD226",
    "LD",
    "IIGS-4",
    "IIGS-0-4",
    "IIGS-5",
    "IIGS-0-5",
    "IIGS05truetrue",
    "IIGS-1-5",
    "IIGS-0-13",
    "IIGS013truetrue",
    "IIGS-1-13",
    "IIGS-13",
    "IIGS",
    "PTTT"
]


game_values = { # for player 0
    "B-RPS": 0.3235, # exact value
    "GP-3322": -0.11262621627252051, # 5000 iters of CFR
    "GP": -0.11262621627252051, # 5000 iters of CFR
    "GP-4644": 0, # how knows
    "LD-116": -0.027693493122754417, # 5000 iters of CFR
    "LD": -0.027693493122754417, # 5000 iters of CFR
    "LD-226": 0,
    "IIGS-4": 0,
    "IIGS-4-ignore": 0,
    "IIGS-5": 0, # exact value
    "IIGS": 0, # exact value
    "IIGS-13": 0,
    "PTTT": 0,
}

rnd_player_expl1_games = {
    "IIGS-5": 0.775,
    "LD-116": 0.795491622574956,
    "GP-3322": 6.796130952380951,
}


import matplotlib as mpl

mpl.use('pdf')
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
plt.rc('font', family='Linux Libertine', size=8)
plt.rc('text', usetex=False)
plt.rc('xtick', labelsize=6)
plt.rc('ytick', labelsize=6)
plt.rc('axes', labelsize=8)
plt.rc('legend', fontsize=6)
plt.rc('legend', handlelength=1)
# 'legend.handlelength': 2

tableau20 = [
          (31, 119, 180), (174, 199, 232), (255, 127, 14), (255, 187, 120),
          (44, 160, 44), (152, 223, 138), (214, 39, 40), (255, 152, 150),
          (148, 103, 189), (197, 176, 213), (140, 86, 75), (196, 156, 148),
          (227, 119, 194), (247, 182, 210), (127, 127, 127), (199, 199, 199),
          (188, 189, 34), (219, 219, 141), (23, 190, 207), (158, 218, 229)]
colors =[]
for (r,g,b) in tableau20:
    colors.append((r/255.,g/255.,b/255.))
cmap = "cool"

def nicelim(min, max):
    return pow(10, np.floor(np.log10(min))), pow(10, np.ceil(np.log10(max)))


def get_game(filename):
    return [g for g in games if g in filename][0]
