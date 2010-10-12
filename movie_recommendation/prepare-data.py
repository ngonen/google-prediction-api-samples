#!/usr/bin/python2.4
#
# Copyright (c) 2010 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

"""Prepare historical movie rating data for a particular user.

Usage:
  prepare-data.py user_rating_filename movie_genre_filename user_id
"""

import string
import sys

__author__ = 'Max Lin'


def parse_genre(genre_filename):
  movieid_to_genre_map = {}
  for line in file(genre_filename):
    line = line.rstrip()
    pieces = line.split('|')

    movie_id = pieces[0]
    genre = pieces[5:24]

    movieid_to_genre_map[movie_id] = genre

  return movieid_to_genre_map


def extract_user_rating_data(train_filename,
                             target_user_id,
                             movieid_to_genre_map):
  for line in file(train_filename):
    line = line.rstrip()

    pieces = line.split();
    user_id = pieces[0]
    movie_id = pieces[1]
    rating = pieces[2]

    if user_id == target_user_id:
      print ([rating] + movieid_to_genre_map[movie_id]).join(',')

def main():
  train_filename = sys.argv[1]
  genre_filename = sys.argv[2]
  target_user_id = sys.argv[3]

  movieid_to_genre_map = parse_genre(genre_filename)
  extract_user_rating_data(train_filename, target_user_id, movieid_to_genre_map)

if __name__ == '__main__':
  main()
