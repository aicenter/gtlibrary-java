from common import *
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import sys
from os.path import basename


game = sys.argv[1]
reset = True if sys.argv[2] == "reset" else False
small = True if sys.argv[3] == "small" else False
savefile = f"plots/exploration/{game}_{sys.argv[2]}_{'small' if small else 'large'}.pdf"
readfile = "processed_data/exploration/%s.csv" % game
title = game

if small:
    fig, axes = plt.subplots(1, 1, figsize=(3, 1.5))
else:
    fig, axes = plt.subplots(1, 1, figsize=(3, 3))

# ylim = (1e-1, 1)
df_all = pd.read_csv(readfile)
df_all["expl1_avg"] /= max_utils[game]
explorations = sorted(df_all["epsExploration"].drop_duplicates().values)

ymin = 1000
ymax = -1000
for expl in explorations:
    df = df_all[(df_all["epsExploration"] == expl) & (df_all["resetData"] == reset)]
    label = f"$\\epsilon = {expl}$"

    df_gadget = df.groupby(["iterationsPerGadgetGame", "iterationsInRoot"]) \
        .apply(lambda x: x.sort_values(["seed"], ascending=False).iloc[0])

    x = df_gadget["expl1_avg"].index.get_level_values(0)
    y = df_gadget["expl1_avg"].values
    if min(y) < ymin:
        ymin = min(y)
    if max(y) > ymax:
        ymax = max(y)

    axes.loglog(x,y, "-", label=label)


axes.set_xlabel("Samples in gadget")
axes.set_ylabel("$expl_1(\\bar{\\bar{\\sigma}})$")


axes.set_ylim(nicelim(ymin, ymax))

plt.legend(loc="lower left")
plt.tight_layout()
if small:
    plt.subplots_adjust(bottom=0.24, top=0.95, left=0.22, right=0.98)
else:
    plt.subplots_adjust(bottom=0.2, top=0.96, left=0.22, right=0.98)
plt.savefig(savefile)
print(f"Saved to {savefile}")
