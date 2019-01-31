import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from common import max_utils

savefile = "plots/cfv_gadget/B-RPS_comparison.pdf"
readfile = "processed_data/cfv_gadget/B-RPS.csv"

df = pd.read_csv(readfile, sep=";")
max_util = max_utils["B-RPS"]

columns = df.columns
ps_cols = [int(col.split("_")[-1]) for col in columns if col.startswith("ps_")]
d_cfv_cols = {
    ps: sorted([col for col in columns if col.startswith("is_cfv_" + str(ps) + "_")])
    for ps in ps_cols
}
d_cfv2_cols = {
    ps: sorted([col for col in columns if col.startswith("is_cfv2_" + str(ps) + "_")])
    for ps in ps_cols
}
d_cfv3_cols = {
    ps: sorted([col for col in columns if col.startswith("is_cfv3_" + str(ps) + "_")])
    for ps in ps_cols
}

cfv_cols =  [col for col in columns if col.startswith("is_cfv_")]
cfv2_cols = [col for col in columns if col.startswith("is_cfv2_")]
cfv3_cols = [col for col in columns if col.startswith("is_cfv3_")]

df[cfv_cols] /= 100
df[cfv2_cols] /= 100

df[cfv_cols] = np.abs(df[cfv_cols].values   - (.003235))
df[cfv2_cols] = np.abs(df[cfv2_cols].values - (.003235))

g = df.groupby("iterations")
expl_cols = 1 if 'expl' in df.columns else 0
fig, axes = plt.subplots(1, 1, figsize=(3.35, 1.25))


x = g['iterations'].first().values
df['expl'] /= (max_util * 2.)  # normalize expl by max utils
df['expl'] /= (max_util * 2.)  # normalize expl by max utils

### expl
# ax = axes[0]
# y = g['expl'].mean().values
# yerr = g['expl'].std().values
# upper = y + yerr
# lower = pd.Series(y - yerr)
# lower[lower < 0] = np.nan
# lower = lower.fillna(method="ffill").values
#
# ax.loglog(x, y)
# ax.fill_between(x, lower, upper, alpha=0.5)
# ax.set_ylabel("$\\overline{expl(\\bar{\\sigma})}$")

seeds = df["seed"].unique().tolist()
ylim = (1e-7, 1)

### weighted
ax = axes
showcols = d_cfv_cols[527]
# ax.plot([x[0], x[-1]], [1,1], "k--")

action = ["Rock", "Paper", "Scissors"]
colors = ["r", "g", "b"]
for j, col in enumerate(showcols):
    color = colors[j]

    # y = g[col].mean().values
    # yerr = g[col].std().values
    y = g[col].apply(lambda x : x[(x <= x.quantile(0.85)) & (x >= x.quantile(0.15))].mean()).values
    yerr = g[col].apply(lambda x : x[(x <= x.quantile(0.85)) & (x >= x.quantile(0.15))].std()).values
    upper = y + yerr
    lower = pd.Series(y - yerr)
    lower[lower < 0] = np.nan
    lower = lower.fillna(method="ffill").values
    ax.loglog(x, y, color=color, label=action[j])
    # ax.fill_between(x, lower, upper, alpha=0.3, edgecolor=color, facecolor=color)

ax.set_ylabel("$\Delta v(t)$")
ax.set_ylim(ylim)
ax.tick_params(axis="both", which="minor", length=0)

### time
showcols = d_cfv2_cols[527]
# ax.plot([x[0], x[-1]], [1,1], "k--")

for j, col in enumerate(showcols):
    color = colors[j]

    # y = g[col].mean().values
    # yerr = g[col].std().values
    y = g[col].apply(lambda x : x[(x <= x.quantile(0.85)) & (x >= x.quantile(0.15))].mean()).values
    yerr = g[col].apply(lambda x : x[(x <= x.quantile(0.85)) & (x >= x.quantile(0.15))].std()).values
    upper = y + yerr
    lower = pd.Series(y - yerr)
    lower[lower < 0] = np.nan
    lower = lower.fillna(method="ffill").values
    ax.loglog(x, y, color=color, linestyle="dashed") #, label="avg "+action[j])
    # ax.fill_between(x, lower, upper, alpha=0.3, edgecolor=color, facecolor=color)

ax.set_ylabel("$\Delta v(t)$")
ax.set_xlabel("Number of samples")
ax.set_ylim(ylim)

# axes[0].set_yticks([1e-5, 100])
# axes[0].set_adjustable('box-forced')
axes.set_adjustable('box-forced')

xlim = (x[0], x[-1])
# axes[0].set_xlim(xlim)
axes.set_xlim(xlim)
axes.set_xscale("log")
axes.set_yscale("log")

# box = ax.get_position()
# ax.set_position([box.x0, box.y0, box.width * 0.8, box.height])
# Put a legend to the right of the current axis
ax.legend(loc='lower left') #, bbox_to_anchor=(1, 0.5))

plt.tight_layout()
plt.subplots_adjust(hspace=0.1, bottom=0.3, top=0.96, left=0.17, right=0.98)
plt.savefig(savefile)
print(f"Saved to {savefile}")
