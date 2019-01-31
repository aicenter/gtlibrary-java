from common import *
import matplotlib.pyplot as plt
import pandas as pd
import sys
from os.path import exists

games = sys.argv[1:]

savefile = f"plots/expl_time/paper_expl.pdf"
fig, axes = plt.subplots(1, 3, figsize=(5, 1.5), sharey=True)
mccfr = True
oos = False

for i, game in enumerate(games):
# for i, game in enumerate(["IIGS"]):
    print(game)
    mccrfile = f"processed_data/expl_time/{game}_MCCR_keep.csv"
    oosfile = f"processed_data/expl_time/{game}_OOS_PST.csv"
    if exists(oosfile):
        oos = True
    mccrresetfile = f"processed_data/expl_time/{game}_MCCR_reset.csv"
    if mccfr:
        mccfrfile = f"processed_data/expl_time/{game}_MCCFR.csv"

    max_util = max_utils[game]
    game_value = game_values[game]

    rnd_player_expl1 = rnd_player_expl1_games[game]
    mccr_df = pd.read_csv(mccrfile)
    if oos:
        oos_df = pd.read_csv(oosfile)
    mccrreset_df = pd.read_csv(mccrresetfile)
    if mccfr:
        mccfr_df = pd.read_csv(mccfrfile)

    # if mccr_df.isnull().values.any() or mccrreset_df.isnull().values.any() or mccfr_df.isnull().values.any():
    #     raise Exception


    col = "expl1_avg"

    max_seed = min(
        mccr_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),
        mccrreset_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),
    )
    if oos:
        max_seed = min(max_seed, oos_df.groupby("iterationsPerGadgetGame")["seed"].max().min())
    if mccfr:
        max_seed = min(max_seed, mccfr_df.groupby("iterationsPerGadgetGame")["seed"].max().min())
    print(f"Max seed {max_seed}")

    print(mccr_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),)
    if oos:
        print(oos_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),)
    print(mccrreset_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),)
    if mccfr:
        print(mccfr_df.groupby("iterationsPerGadgetGame")["seed"].max().min(),)


    x = sorted(pd.concat((
        # oos_df["iterationsPerGadgetGame"],
        mccr_df["iterationsPerGadgetGame"],
        mccrreset_df["iterationsPerGadgetGame"],
        # mccfr_df["iterationsPerGadgetGame"]
    ), axis=0).drop_duplicates().values)

    mccr_df = mccr_df.loc[mccr_df["seed"] == max_seed].set_index("iterationsPerGadgetGame")
    if oos:
        oos_df = oos_df.loc[oos_df["seed"] == max_seed].set_index("iterationsPerGadgetGame")
    mccrreset_df = mccrreset_df.loc[mccrreset_df["seed"] == max_seed].set_index("iterationsPerGadgetGame")
    if mccfr:
        mccfr_df = mccfr_df.loc[mccfr_df["seed"] == max_seed].set_index("iterationsPerGadgetGame")

    y_mccr = mccr_df.loc[x, col] / max_util - game_value
    if oos:
        y_oos = oos_df.loc[x, col] / max_util - game_value
    y_mccrreset = mccrreset_df.loc[x, col] / max_util - game_value
    rnd_player_expl1 = rnd_player_expl1 / max_util - game_value
    if mccfr:
        y_mccfr = mccfr_df.loc[x, col] / max_util - game_value

    print(f"max_util {max_util} game_value {game_value}")

    print(y_mccr)
    if oos:
        print(y_oos)
    print(y_mccrreset)

    ax = axes[i]
    ax.loglog(x, y_mccrreset, color=colors[0], label="MCCR (reset)")
    ax.loglog(x, y_mccr,color=colors[1*2], label="MCCR (keep)")
    if oos:
        ax.loglog(x, y_oos, color=colors[2*2], label="OOS (PST)")
    if mccfr:
        print(y_mccfr)
        ax.loglog(x, y_mccfr, color=colors[3*2], label="MCCFR")
    ax.loglog((x[0], x[-1]), (rnd_player_expl1, rnd_player_expl1), color=colors[4*2], label="RND")


axes[0].set_title("IIGS-5")
axes[1].set_title("LD-116")
axes[2].set_title("GP-3322")

axes[0].set_ylabel("$expl_2(\\bar{\\bar{\\sigma}})$")
axes[1].set_xlabel("Time [ms]")

axes[0].tick_params(axis="both", which="minor", length=0)
axes[1].tick_params(axis="both", which="minor", length=0)
axes[2].tick_params(axis="both", which="minor", length=0)
axes[0].tick_params(axis="both", which="major", length=3)
axes[1].tick_params(axis="both", which="major", length=3)
axes[2].tick_params(axis="both", which="major", length=3)

box = axes[2].get_position()
axes[2].set_position([box.x0, box.y0, box.width * 0.8, box.height])
# Put a legend to the right of the current axis
axes[2].legend(loc='center left', bbox_to_anchor=(1, 0.5))

ylim = [2e-2, 1]
axes[0].set_ylim(ylim)
axes[1].set_ylim(ylim)
axes[2].set_ylim(ylim)


plt.tight_layout()
plt.subplots_adjust(wspace=0.09, top=0.85, bottom=0.23, left=0.10, right=0.83)
print(f"Saved to {savefile}")
plt.savefig(savefile)
