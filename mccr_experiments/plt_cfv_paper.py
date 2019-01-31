import pickle
import sys

from common import *

games = sys.argv[1:]

plt.rc('axes', labelsize=7)
plt.rc('legend', handlelength=2)

# cfv_vals = "cfv_ordinary_abs"
# cfv_vals = "cfv_weighted_abs"
# cfv_vals = "cfv_exact_abs"
# cfv_vals = "cfv_weighted_rel"
cfv_vals = "cfv_weighted_abs"

savefile = f"plots/cfv_gadget/cfv_paper.pdf"

with open("processed_data/cfv_gadget/agg.pickle", "rb") as ff:
    agg = pickle.load(ff)

fig, axes = plt.subplots(2, 1, figsize=(3,3), sharex=True, gridspec_kw = {'height_ratios':[1, 2]})

x = list(range(1000))
for game, vals in agg.items():
    if len(vals["x"]) < len(x):
        x = vals["x"]
maxx = x[-1]
print(maxx)

plot_args = {
    "B-RPS":    dict(linewidth=1, linestyle="-",  color=colors[0]), #colors[0]),
    "PTTT":     dict(linewidth=1, linestyle="--", color=colors[14]), #colors[2]),
    "IIGS-5":   dict(linewidth=1, linestyle="-",  color=colors[2]), #colors[4]),
    "IIGS-13":  dict(linewidth=1, linestyle="--", color=colors[4]), #colors[6]),
    "LD-116":   dict(linewidth=1, linestyle="-",  color=colors[6]), #colors[8]),
    "LD-226":   dict(linewidth=1, linestyle="--", color=colors[8]), #colors[10]),
    "GP-3322":  dict(linewidth=1, linestyle="-",  color=colors[10]), #colors[12]),
    "GP-4644":  dict(linewidth=1, linestyle="--", color=colors[12]), #colors[14]),
}

for game in games:
    vals = agg[game]
    print(game)
    x = vals["x"]
    args = plot_args[game]
    if "expl" in vals:
        y, yerr = vals["expl"]
        ax = axes[0]
        ax.loglog(x, y, label=game, **args)

    if cfv_vals in vals:
        y, yerr = vals[cfv_vals]

        ax = axes[1]
        if "abs" in cfv_vals:
            ax.plot(x, y, label=game, **args)
        else:
            ax.plot(x, y, label=game, **args)

axes[-1].set_xlabel("Number of samples")
axes[0].set_ylabel("$\overline{\expl(\\bar{\sigma}^t)}$")
axes[1].set_ylabel("Averages of CFVs differences")

axes[0].tick_params(axis="both", which="minor", length=0)
axes[1].tick_params(axis="both", which="minor", length=0)
axes[0].tick_params(axis="both", which="major", length=3)
axes[1].tick_params(axis="both", which="major", length=3)

box = axes[-1].get_position()
axes[-1].set_position([box.x0, box.y0 + box.height * 0.3,
                 box.width, box.height * 0.7])

axes[-1].legend(loc='upper center', bbox_to_anchor=(0.4, -0.23), shadow=False, ncol=4)


axes[0].set_ylim(1e-3, 1)
if "abs" in cfv_vals:
    axes[1].set_ylim(-0.002, 0.2)
else:
    axes[1].set_ylim(-0.025, 0.10)

axes[0].spines['right'].set_visible(False)
axes[1].spines['right'].set_visible(False)
axes[0].spines['top'].set_visible(False)
axes[1].spines['top'].set_visible(False)

axes[0].get_yaxis().set_label_coords(-0.155, 0.5)
axes[1].get_yaxis().set_label_coords(-0.155, 0.5)

ax.set_xlim((x[0], x[-8]))

plt.subplots_adjust(hspace=0.12, top=0.98, bottom=0.23, left=0.175, right=0.98)
plt.savefig(savefile)
print(f"Saved to {savefile}")
