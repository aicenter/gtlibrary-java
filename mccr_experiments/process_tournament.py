from common import *
import pandas as pd
import sys

pd.set_option("display.max_columns", -1)
pd.set_option("display.width", -1)
pd.set_option('display.max_colwidth', -1)

conf_level = 99
conf_interval = {
    80: 1.282,
    85: 1.440,
    90: 1.645,
    95: 1.960,
    99: 2.576,
    99.5: 2.807,
    99.9: 3.291,
}

match_files = sys.argv[1:]

def parse_moves(x):
    moves = x.split("Move{")
    parsed = []
    for move in moves[1:]:
        move = move.replace("}, ", "")
        move = move.replace("},", "")
        move = move.replace("}", "")

        data = move.split(", ")
        # print(move)
        player=float(data[0].split("=")[1])
        action=data[1]
        n=1
        if action.count("'") == 1:
            while action.count("'") != 2:
                action+=", "+data[n+1]
                n+=1
        action = str((action.split("=")[1])[1:-1])
        prob=float(data[(n+2-1)].split("=")[1])
        numSamplesDuringRun=float(data[(n+3-1)].split("=")[1])
        numSamplesInCurrentIS=float(data[(n+4-1)].split("=")[1])
        numNodesTouchedDuringRun=float(data[(n+5-1)].split("=")[1])
        p_dist=(", ".join(data[(n+6-1):])).split("=")[1]

        parsed.append(dict(
            player=player,
            action=action,
            numSamplesDuringRun=numSamplesDuringRun,
            numSamplesInCurrentIS=numSamplesInCurrentIS,
            numNodesTouchedDuringRun=numNodesTouchedDuringRun,
            prob=prob,
            p_dist  =p_dist,
        ))

    return parsed

def key_sort_alg(a):
    if a[0].startswith("MCCR"):
        return "aa"+a[0]
    if "MCCFR" in a[0]:
        return "bb"+a[0]
    if "PST" in a[0]:
        return "cc"+a[0]
    if "IST" in a[0]:
        return "dd"+a[0]
    if a[0].startswith("RND"):
        return "zz"+a[0]
    return "gg"+a[0]

def sort_df(df, column_idx, key):
    '''Takes dataframe, column index and custom function for sorting,
    returns dataframe sorted by this column using this function'''

    col = df.ix[:,column_idx]
    temp = np.array(col.values.tolist())
    order = sorted(range(len(temp)), key=lambda j: key(temp[j]))
    return df.ix[order]

def util_result(x):
    conf = f"{x['conf_utils']:.1f}".rjust(4, "~").replace("~", "\\nobreakspace")
    util = f"{x['utils']:.1f}".rjust(5, "~").replace("~", "\\nobreakspace")
    res = util +" $\\pm$ "+conf
    if not ((x['utils'] <= 0 and (x['utils'] + x['conf_utils']) < 0) \
         or (x['utils'] >= 0 and (x['utils'] - x['conf_utils']) > 0)):
        if x['utils'] >= 0:
            res = "{\\color{insignwin} %s}" % res
        else:
            res = "{\\color{insignlose} %s}" % res
    else:
        if x['utils'] >= 0:
            res = "{\\color{win} %s}" % res
        else:
            res = "{\\color{lose} %s}" % res
        # res = "\\textbf{%s}" % res
    return res

for match_file in match_files:
    game = [g for g in max_utils.keys() if g in match_file][0]
    max_util = max_utils[game]
    print(match_file +" "+ game, file=sys.stderr)

    df = pd.read_csv(match_file, sep=";")
    df.loc[df["alg1"] == "MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget", "alg1"] = "MCCR (keep)"
    df.loc[df["alg2"] == "MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget", "alg2"] = "MCCR (keep)"
    df.loc[df["alg1"] == "MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget_buildGadget", "alg1"] = "MCCR (keep)"
    df.loc[df["alg2"] == "MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget_buildGadget", "alg2"] = "MCCR (keep)"

    df.loc[df["alg1"] == "MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget", "alg1"] = "MCCR (reset)"
    df.loc[df["alg2"] == "MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget", "alg2"] = "MCCR (reset)"
    df.loc[df["alg1"] == "MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget_buildGadget", "alg1"] = "MCCR (reset)"
    df.loc[df["alg2"] == "MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget_buildGadget", "alg2"] = "MCCR (reset)"

    df.loc[df["alg1"] == "OOS", "alg1"] = "OOS (IST)"
    df.loc[df["alg2"] == "OOS", "alg2"] = "OOS (IST)"
    df.loc[df["alg1"] == "OOS_PST", "alg1"] = "OOS (PST)"
    df.loc[df["alg2"] == "OOS_PST", "alg2"] = "OOS (PST)"
    df[['utils1', 'utils2']] /= max_util
    # df['pm'] = df['moves'].apply(parse_moves)

    primes = [2,3,5,7,9,11,13,17,19]
    algs = list(set(list(df["alg1"].values) + list(df["alg2"].values)))
    algs_idx = dict(zip(algs, primes[:len(algs)]))
    df["alg1_idx"] =df["alg1"].apply(lambda x: algs_idx[x])
    df["alg2_idx"] =df["alg2"].apply(lambda x: algs_idx[x])
    df["alg_comb_idx"] =  df["alg1_idx"]*df["alg2_idx"]

    pairs = df[["alg1", "alg2"]].drop_duplicates().values
    pairs = sorted(list(pairs), key=key_sort_alg)
    ref_algs = set()
    for alg1,alg2 in pairs:
        if not (alg1,alg2) in ref_algs and not (alg2,alg1) in ref_algs :
            ref_algs.add((alg1,alg2))
    df["alg1ref"] = df[["alg1", "alg2"]].apply(lambda x: (x["alg1"],x["alg2"]) in ref_algs, axis=1).astype(int)
    combinations = df[df["alg1ref"] == 1][["alg1", "alg2", "alg_comb_idx"]].drop_duplicates().set_index("alg_comb_idx")

    algs_reduced = df.groupby("alg_comb_idx").apply(lambda x: x["utils1"]*x["alg1ref"]+x["utils2"]*(1-x["alg1ref"])).reset_index()
    algs_reduced["utils"] = algs_reduced[0]*100
    algs_reduced[["alg1", "alg2"]] = algs_reduced["alg_comb_idx"].apply(lambda x: combinations.loc[x])


    alg1_vs_alg2 = algs_reduced.groupby(["alg1", "alg2"])[["utils"]].mean()
    alg1_vs_alg2["std_utils"] = algs_reduced.groupby(["alg1", "alg2"])["utils"].std()
    alg1_vs_alg2["num_matches"] = algs_reduced.groupby(["alg1", "alg2"])[["utils"]].size()[0]
    # print(alg1_vs_alg2["num_matches"], file=sys.stderr)

    alg1_vs_alg2["conf_utils"] = alg1_vs_alg2["std_utils"] / np.sqrt(alg1_vs_alg2["num_matches"]) * conf_interval[conf_level]
    alg1_vs_alg2["utils_low"] = alg1_vs_alg2["utils"] - alg1_vs_alg2["conf_utils"]
    alg1_vs_alg2["utils_upp"] = alg1_vs_alg2["utils"] + alg1_vs_alg2["conf_utils"]

    utils_conf = alg1_vs_alg2[["utils", "conf_utils"]].apply(util_result, axis=1).reset_index()
    utils_conf["alg"] = utils_conf[["alg1", "alg2"]].apply(lambda x: (x["alg1"],x["alg2"]), axis=1)
    utils_conf = sort_df(utils_conf, "alg", key_sort_alg)
    table = utils_conf.pivot("alg1", "alg2", 0)

    # c = ['MCCR_reset', 'MCCFR', 'OOS', 'OOS_PST', 'RM', 'UCT', 'RND']
    # r = ['MCCR_keep', 'MCCR_reset', 'MCCFR', 'OOS', 'OOS_PST', 'RM', 'UCT']
    # print(utils_conf, file=sys.stderr)
    c = ['MCCR (reset)', 'MCCFR', 'OOS (PST)', 'OOS (IST)', 'RM', 'UCT', 'RND']
    r = ['MCCR (keep)', 'MCCR (reset)', 'MCCFR', 'OOS (PST)', 'OOS (IST)', 'RM', 'UCT']
    table=table[c].loc[r]
    # print(table)
    print(table.fillna("").to_latex(escape=False))

    for alg1,alg2 in ref_algs:
        print(alg1, alg2, ((algs_reduced["alg1"] == alg1) & (algs_reduced["alg2"] == alg2)).sum(), file=sys.stderr)

