from common import max_utils
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import pandas as pd
import sys
import numpy as np

game = sys.argv[1]
cfvs = sys.argv[2]
small = True if sys.argv[3]  == "small" else False
fn = np.abs if sys.argv[4] == "abs" else lambda x: x

assert cfvs == "weighted" or cfvs == "divt" or cfvs == "precise"
savefile = f"plots/cfv_gadget/{game}_summary_{cfvs}_{'small' if small else 'large'}_{sys.argv[4]}.pdf"
readfile = "processed_data/cfv_gadget/%s.csv" % game
title = game


colors = list(mcolors.BASE_COLORS.values()) + list(mcolors.CSS4_COLORS.values())

df = pd.read_csv(readfile, sep=";")
max_util = max_utils[title]

columns = df.columns
ps_cols = [int(col.split("_")[-1]) for col in columns if col.startswith("ps_")]
if cfvs == "weighted":
    cfv_cols = [col for col in columns if col.startswith("is_cfv_")]
elif cfvs == "divt":
    cfv_cols = [col for col in columns if col.startswith("is_cfv2_")]
elif cfvs == "precise":
    cfv_cols = [col for col in columns if col.startswith("is_cfv3_")]


maxIters = df.groupby('seed')['iterations'].max().min()
df = df[df['iterations'] <= maxIters]
# df[cfv_cols] = df[cfv_cols].astype(np.double)
df[cfv_cols] /= max_util
g = df.groupby("iterations")
x = g['iterations'].first().values

expl_cols = 1 if 'expl' in df.columns else 0
ratios = [1, 2] if expl_cols else [1]
if small:
    fig, axes = plt.subplots(expl_cols+1, 1, figsize=(2.35+expl_cols, 2), sharex=True, gridspec_kw = {'height_ratios':ratios})
else:
    fig, axes = plt.subplots(expl_cols+1, 1, figsize=(3.35, 3), sharex=True, gridspec_kw = {'height_ratios':ratios})



if 'expl' in df.columns:
    df['expl'] /= (max_util * 2.)  # normalize expl by max utils
    ax = axes[0]
    y = g['expl'].mean().values
    yerr = g['expl'].std().values
    upper = y + yerr
    lower = pd.Series(y - yerr)
    lower[lower < 0] = np.nan
    lower = lower.fillna(method="ffill").values


    ax.loglog(x, y)
    ax.fill_between(x, lower, upper, alpha=0.5)
    ax.set_ylabel("$\\overline{expl(\\bar{\\sigma})}$")
    ax.set_xlim((x[0], x[-1]))


if 'expl' in df.columns:
    ax = axes[1]
else:
    ax = axes
color = colors[0]



# targets = df.loc[df['iterations'] == maxIters, cfv_cols+['seed']].set_index("seed")
# values = df.groupby("seed", as_index=False).apply(lambda x: pd.concat((x.sort_values("iterations")[cfv_cols] - targets.loc[x["seed"].iloc[0]], x[['seed', 'iterations']]), axis=1)).reset_index(drop=True)
targets = df.loc[df['iterations'] == maxIters, cfv_cols+['seed']].set_index("seed")
values = df.groupby("seed").apply(
    lambda x: pd.concat((
        fn(x.sort_values("iterations")[cfv_cols] - targets.loc[x['seed'].iloc[0]]),
        x[['seed', 'iterations']]), axis=1)
).reset_index(drop=True)
g = values.groupby("iterations")
avg_cfvs = g[cfv_cols].mean()
y = avg_cfvs.mean(axis=1)
# y = g[cfv_cols].mean().values
yerr = avg_cfvs.std(axis=1).values


positions=x[2::4]
avg_cfvs=avg_cfvs.iloc[2::4, :]
w = 0.2
width = lambda p, w: 10**(np.log10(p)+w/2.)-10**(np.log10(p)-w/2.)
ax.plot([x[0], x[-1]], [0,0], "#666666", linestyle="--")

if y.max() > 1:
    ax.plot([x[0], x[-1]], [1,1], "#666666", linestyle="--")

ax.boxplot(avg_cfvs, positions=positions, widths=width(positions,w), sym='')
ax.set_xscale("log")
ax.semilogx(x, y, color=color)
ax.fill_between(x, y - yerr, y + yerr, alpha=0.3, edgecolor=color, facecolor=color)
# ax.set_ylabel("$v^{\\bar{\\sigma}^T}_{-i}(\\tilde{I})$")
ax.set_xlabel("Number of samples")
ax.set_ylabel("$\\Delta v(t)$")
ax.set_xlim((x[0], x[-1]))
# ax.set_ylim((0,1))
plt.tight_layout()
if small:
    plt.subplots_adjust(hspace=0.1, bottom=0.2, top=0.96, left=0.16, right=0.98)
else:
    plt.subplots_adjust(top=0.94, bottom=0.06, left=0.16, right=0.98)
plt.savefig(savefile)
print(f"Saved to {savefile}")
