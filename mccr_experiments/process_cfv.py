import pickle
import sys

import pandas as pd

from common import *

games = sys.argv[1:]


def calc_vals(df, cfv_cols, fn):
    targets = df.loc[df['iterations'] == maxIters, cfv_cols + ['seed']].set_index("seed")
    values = df.groupby("seed").apply(
        lambda x: pd.concat((
            fn(x.sort_values("iterations")[cfv_cols] - targets.loc[x['seed'].iloc[0]]),
            x[['seed', 'iterations']]), axis=1)
    ).reset_index(drop=True)
    g = values.groupby("iterations")
    avg_cfvs = g[cfv_cols].mean()

    y = avg_cfvs.mean(axis=1).values
    yerr = avg_cfvs.std(axis=1).values
    return y, yerr


# find max iters cutoff
x = list(range(1000))
for game in games:
    max_util = max_utils[game]

    file = f"processed_data/cfv_gadget/{game}.csv"
    print(f"Reading {file}")
    df = pd.read_csv(file, sep=";")

    maxIters = df.groupby('seed')['iterations'].max().min()
    df = df[df['iterations'] <= maxIters]

    g = df.groupby("iterations")
    xnew = g['iterations'].first().values
    if len(xnew) < len(x):
        x = xnew
        print(x[-1])

maxIters = x[-1]
print(f"Cuttoff at {maxIters}")

agg = dict()
for game in games:
    cur_agg = dict()
    max_util = max_utils[game]

    file = f"processed_data/cfv_gadget/{game}.csv"
    print(f"Reading {file}")
    df = pd.read_csv(file, sep=";")

    columns = df.columns
    ps_cols = [int(col.split("_")[-1]) for col in columns if col.startswith("ps_")]
    cfv_cols = [col for col in columns if col.startswith("is_cfv_")]
    cfv2_cols = [col for col in columns if col.startswith("is_cfv2_")]
    cfv3_cols = [col for col in columns if col.startswith("is_cfv3_")]

    df = df[df['iterations'] <= maxIters]

    df[cfv_cols] /= max_util
    df[cfv2_cols] /= max_util
    df[cfv3_cols] /= max_util

    g = df.groupby("iterations")
    x = g['iterations'].first().values

    cur_agg["x"] = x

    expl_cols = 1 if 'expl' in df.columns else 0
    if 'expl' in df.columns:
        df['expl'] /= (max_util * 2.)  # normalize expl by max utils
        y = g['expl'].mean().values
        yerr = g['expl'].std().values

        cur_agg["expl"] = [y, yerr]

    identity = lambda x: x
    cur_agg["cfv_ordinary_abs"] = calc_vals(df, cfv2_cols, np.abs)
    cur_agg["cfv_ordinary_rel"] = calc_vals(df, cfv2_cols, identity)
    cur_agg["cfv_weighted_abs"] = calc_vals(df, cfv_cols, np.abs)
    cur_agg["cfv_weighted_rel"] = calc_vals(df, cfv_cols, identity)
    if cfv3_cols:
        cur_agg["cfv_exact_abs"] = calc_vals(df, cfv3_cols, np.abs)
        cur_agg["cfv_exact_rel"] = calc_vals(df, cfv3_cols, identity)

    agg[game] = cur_agg

print("Writing pickle")
with open("processed_data/cfv_gadget/agg.pickle", "wb") as ff:
    pickle.dump(agg, ff)
